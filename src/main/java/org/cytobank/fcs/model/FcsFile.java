package org.cytobank.fcs.model;

import com.google.gson.GsonBuilder;
import lombok.Data;

import java.nio.charset.Charset;

@Data
public class FcsFile {

  public static final String ENCODING = "ISO-8859-1";
  public static final Charset charset = Charset.forName(ENCODING);

  private HeaderSegment headerSegment;
  private TextSegment textSegment;

  private char delimiter;

  @Override
  public String toString() {
    return new GsonBuilder().serializeNulls().create().toJson(this);
  }

}
