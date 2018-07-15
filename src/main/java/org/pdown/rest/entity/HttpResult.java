package org.pdown.rest.entity;

public class HttpResult {

  private int code;
  private String msg;

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }


  public HttpResult code(int code) {
    this.code = code;
    return this;
  }

  public HttpResult msg(String msg) {
    this.msg = msg;
    return this;
  }

  @Override
  public String toString() {
    return "HttpResult{" +
        "code=" + code +
        ", msg='" + msg + '\'' +
        '}';
  }
}
