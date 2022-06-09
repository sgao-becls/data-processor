package org.cytobank.fcs.exception;

public class ParseTextSegmentException extends RuntimeException {
  public ParseTextSegmentException(Exception e) {
    super(e);
  }

  public ParseTextSegmentException(String message) {
    super(message);
  }
}
