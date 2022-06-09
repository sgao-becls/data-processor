package test.org.cytobank.fcs;

import org.cytobank.fcs.FcsFileParser;
import org.cytobank.fcs.model.FcsFile;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

public class FcsFileParserTest {

  @Test
  public void constructionTest() {
    String testFilePath = "/Users/sgao/tmp/fcs/pbmc_lrs005_il10.fcs";

    try (FileInputStream inputStream = new FileInputStream(Paths.get(testFilePath).toFile())) {
      FcsFileParser fcsFileParser = new FcsFileParser(inputStream);
      FcsFile fcsFile = fcsFileParser.getFcsFile();
      System.out.println(fcsFile);
    } catch (FileNotFoundException e) {

    } catch (IOException e) {

    }
  }

}
