package org.cytobank.fcs.exception;

public class ParseSegmentRangeException extends RuntimeException {
  public ParseSegmentRangeException(Exception e) {
    super(e);
  }

  public ParseSegmentRangeException(String message) {
    super(message);
  }
}
