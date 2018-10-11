package org.pdown.rest.form;

import java.util.Map;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.entity.HttpResponseInfo;

public class CreateTaskForm {

  private HttpRequestForm request;
  private HttpResponseInfo response;
  private HttpDownConfigInfo config;
  private Map<String,Object> data;

  public HttpRequestForm getRequest() {
    return request;
  }

  public void setRequest(HttpRequestForm request) {
    this.request = request;
  }

  public HttpResponseInfo getResponse() {
    return response;
  }

  public void setResponse(HttpResponseInfo response) {
    this.response = response;
  }

  public HttpDownConfigInfo getConfig() {
    return config;
  }

  public void setConfig(HttpDownConfigInfo config) {
    this.config = config;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public CreateTaskForm setData(Map<String, Object> data) {
    this.data = data;
    return this;
  }
}
