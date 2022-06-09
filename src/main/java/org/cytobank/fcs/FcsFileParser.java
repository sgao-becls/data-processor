package org.cytobank.fcs;

import lombok.Getter;
import org.cytobank.fcs.exception.InvalidFcsFileException;
import org.cytobank.fcs.exception.ParseSegmentRangeException;
import org.cytobank.fcs.exception.ParseTextSegmentException;
import org.cytobank.fcs.exception.ParseVersionException;
import org.cytobank.fcs.exception.SkipBytesException;
import org.cytobank.fcs.model.FcsFile;
import org.cytobank.fcs.model.HeaderSegment;
import org.cytobank.fcs.model.TextSegment;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.util.logging.Logger;

/**
 * Parse a fcs file
 * <p>
 * One parse can only parse one fcs file.
 */
public class FcsFileParser {

  private static final Logger log = Logger.getLogger(FcsFileParser.class.getName());

  private final BufferedInputStream inputStream;

  @Getter
  private final FcsFile fcsFile;
  private final HeaderSegment headerSegment;
  private final TextSegment textSegment;

  private final CharsetDecoder decoder = FcsFile.charset.newDecoder();

  public FcsFileParser(InputStream inputStream) {
    this.inputStream = new BufferedInputStream(inputStream);
    fcsFile = new FcsFile();
    headerSegment = new HeaderSegment();
    textSegment = new TextSegment();
    fcsFile.setHeaderSegment(headerSegment);
    fcsFile.setTextSegment(textSegment);

    parseHeaderSegment();
    parseTextSegment();
  }

  protected void parseTextSegment() {
    skipBytes(headerSegment.getTextSegmentStart() - HeaderSegment.HEADER_SEGMENT_SIZE);
    parseFullTextSegment();
    findDelimiter();
    parseKvpair();
  }

  private void parseKvpair() {
    String[] pairs;
    if (fcsFile.getDelimiter() == '\\') {
      // If the delimiter character is a backslash, then we have to escape
      // it in the regular expression.
      pairs = textSegment.getFullText().split("[\\\\]");
    } else {
      // Otherwise, we can just split it normally by using the character
      // in the regular expression.
      // TODO: why [\\\\] or [\|]
      pairs = textSegment.getFullText().split("[" + fcsFile.getDelimiter() + "]");
    }

    /**
     * Calculate the number of pairs --- The number of pairs is the length of the pairs array minus
     * 1 divided by 2. The one is due to the empty first element from the Java split above.
     */
    int numPairs = (pairs.length - 1) / 2;

    // Loop through the TEXT segment we just split to get the keys and values
    // The key is in (i * 2) + 1 to account for the empty first element.
    // The value is in (i * 2) + 2 to account for the empty first element.
    for (int i = 0; i < numPairs; i++) {
      String key = pairs[(i * 2) + 1].trim();
      String value = pairs[(i * 2) + 2].trim();
      if (value.isEmpty()) {
        // CYT-1267 Preserve space-character keywords
        value = " ";
      }
      textSegment.getKvpair().put(key, value);
    }
  }

  private void findDelimiter() {
    // The first character of the primary TEXT segment contains the
    // delimiter character
    fcsFile.setDelimiter(textSegment.getFullText().charAt(0));
  }

  private void parseFullTextSegment() {
    byte[] bytes = new byte[headerSegment.getTextSegmentSize()];
    int numRead = 0;
    try {
      numRead = inputStream.readNBytes(bytes, 0, bytes.length);
    } catch (IOException e) {
      throw new ParseTextSegmentException(e);
    }
    if(numRead < headerSegment.getTextSegmentSize()) {
      throw new ParseTextSegmentException("FcsFile text segment is too small. Fcsfile could be bad or truncated");
    }
    // Decode text segment bytes and convert them to a String
    try {
      textSegment.setFullText(decoder.decode(ByteBuffer.wrap(bytes)).toString());
    } catch (CharacterCodingException e) {
      throw new ParseTextSegmentException(e);
    }
  }

  protected void parseHeaderSegment() {
    parseVersion();
    skipBytes(HeaderSegment.BLANK_BYTES_SIZE);
    parseSegmentRange();
  }

  private void parseSegmentRange() {
    // Create a byte array to hold the HEADER
    byte[] headerByteArray = new byte[HeaderSegment.SEGMENT_RANGE_SIZE];

    // Read the header into the byte array
    log.info(String.format("Read FcsFile header of size %d", headerByteArray.length));
    int numRead = 0;
    try {
      numRead = inputStream.readNBytes(headerByteArray, 0, headerByteArray.length);
    } catch (IOException e) {
      throw new ParseSegmentRangeException(e);
    }

    if (numRead < 48) {
      throw new ParseSegmentRangeException(
          "FcsFile header is too small. Fcsfile could be bad or truncated");
    }

    headerSegment.setTextSegmentStart(Integer.parseInt((new String(headerByteArray, 0, 8)).trim()));
    headerSegment.setTextSegmentEnd(Integer.parseInt((new String(headerByteArray, 8, 8)).trim()));
    headerSegment.setDataSegmentStart(Long.parseLong((new String(headerByteArray, 16, 8)).trim()));
    headerSegment.setDataSegmentEnd(Long.parseLong((new String(headerByteArray, 24, 8)).trim()));
    headerSegment.setAnalysisSegmentStart(Long.parseLong((new String(headerByteArray, 32, 8)).trim()));
    headerSegment.setAnalysisSegmentEnd(Long.parseLong((new String(headerByteArray, 40, 8)).trim()));

  }

  private void skipBytes(int skippedBytes) {
    if (skippedBytes <= 0) {
      return;
    }
    byte[] skipBytes = new byte[skippedBytes];
    try {
      inputStream.readNBytes(skipBytes, 0, skipBytes.length);
    } catch (IOException e) {
      throw new SkipBytesException(e);
    }
    log.info(String.format("Skipped %d bytes", skippedBytes));
  }

  private void validateFcsFile() {
    if (!headerSegment.getVersion().startsWith(HeaderSegment.FCS_PREFIX)) {
      log.info("Missing Fcsfile version from header. File is not of FcsFile type");
      throw new InvalidFcsFileException(
          "Missing Fcsfile version from header. File is not of FcsFile type");
    }
  }

  private void parseVersion() {
    // - [ ] read version
    log.info("Read FcsFile version");
    byte[] versionByteArray = new byte[HeaderSegment.VERSION_SIZE];

    // Read the version into the byte array
    int numRead = 0;
    try {
      numRead = inputStream.readNBytes(versionByteArray, 0, versionByteArray.length);
    } catch (IOException e) {
      throw new ParseVersionException("IOException when reading fcs file version");
    }
    if (numRead < HeaderSegment.VERSION_SIZE) {
      log.info("Fcs file version size is incorrect. File is not of FcsFile type");
      throw new ParseVersionException("Fcs file version length is too small");
    }
    headerSegment.setVersion(new String(versionByteArray));
    validateFcsFile();
  }


}
