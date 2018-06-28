package org.pdown.rest.entity;

public class HttpResult<T> {

  private T data;
  private String msg;

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public HttpResult data(T data) {
    this.data = data;
    return this;
  }

  public HttpResult msg(String msg) {
    this.msg = msg;
    return this;
  }

  @Override
  public String toString() {
    return "HttpResult{" +
        "data=" + data +
        ", msg='" + msg + '\'' +
        '}';
  }
}
