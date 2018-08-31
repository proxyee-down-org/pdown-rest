package org.pdown.rest.form;

import org.pdown.core.entity.HttpResponseInfo;

public class ResolveForm {

  private HttpRequestForm request;
  private HttpResponseInfo response;

  public ResolveForm() {
  }

  public ResolveForm(HttpRequestForm request, HttpResponseInfo response) {
    this.request = request;
    this.response = response;
  }

  public HttpRequestForm getRequest() {
    return request;
  }

  public ResolveForm setRequest(HttpRequestForm request) {
    this.request = request;
    return this;
  }

  public HttpResponseInfo getResponse() {
    return response;
  }

  public ResolveForm setResponse(HttpResponseInfo response) {
    this.response = response;
    return this;
  }
}
