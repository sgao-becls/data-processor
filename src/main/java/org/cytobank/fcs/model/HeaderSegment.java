package org.cytobank.fcs.model;

import lombok.Data;

@Data
public class HeaderSegment {

  // Size of the version string in bytes
  public static final int VERSION_SIZE = 6;
  public static final int BLANK_BYTES_SIZE = 4;
  public static final int SEGMENT_RANGE_SIZE = 48;
  public static final int HEADER_SEGMENT_SIZE = VERSION_SIZE + BLANK_BYTES_SIZE + SEGMENT_RANGE_SIZE;
  // Default prefix for FCS files to check whether the file is a FCS file.
  public static final String FCS_PREFIX = "FCS";



  private String version;

  private int textSegmentStart;
  private int textSegmentEnd;
  private long dataSegmentStart;
  private long dataSegmentEnd;
  private long analysisSegmentStart;
  private long analysisSegmentEnd;

  public int getTextSegmentSize() {
    return textSegmentEnd - textSegmentStart + 1;
  }

  public long getDataSegmentSize() {
    return dataSegmentEnd - dataSegmentStart + 1;
  }

  public long getAnalysisSegmentSize() {
    return analysisSegmentEnd - analysisSegmentStart + 1;
  }
}
