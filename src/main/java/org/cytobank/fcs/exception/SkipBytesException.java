package org.cytobank.fcs.exception;

public class SkipBytesException extends RuntimeException {
  public SkipBytesException(Exception e) {
    super(e);
  }

  public SkipBytesException(String message) {
    super(message);
  }
}
