package org.pdown.rest.base.exception;

public class ParameterException extends RuntimeException {

  private int code;

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public ParameterException(int code, String message) {
    super(message);
    this.code = code;
  }
}
