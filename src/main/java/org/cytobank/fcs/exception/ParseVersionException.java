package org.cytobank.fcs.exception;

public class ParseVersionException extends RuntimeException {
  public ParseVersionException(Exception e) {
    super(e);
  }

  public ParseVersionException(String message) {
    super(message);
  }
}
