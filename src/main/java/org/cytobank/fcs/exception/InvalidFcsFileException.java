package org.cytobank.fcs.exception;

public class InvalidFcsFileException extends RuntimeException {
  public InvalidFcsFileException(Exception e) {
    super(e);
  }

  public InvalidFcsFileException(String message) {
    super(message);
  }
}
