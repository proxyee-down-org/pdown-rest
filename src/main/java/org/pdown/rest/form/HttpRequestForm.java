package org.pdown.rest.form;

import java.util.Map;
import org.pdown.core.entity.HttpHeadsInfo;
import org.pdown.core.entity.HttpRequestInfo;

public class HttpRequestForm {

  private String method;
  private String url;
  private Map<String, String> heads;
  private String body;

  public HttpRequestForm() {
  }

  public HttpRequestForm(String method, String url, Map<String, String> heads, String body) {
    this.method = method;
    this.url = url;
    this.heads = heads;
    this.body = body;
  }

  public HttpRequestForm(String url, Map<String, String> heads, String body) {
    this("GET", url, heads, body);
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Map<String, String> getHeads() {
    return heads;
  }

  public void setHeads(Map<String, String> heads) {
    this.heads = heads;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public static HttpRequestForm parse(HttpRequestInfo httpRequest) {
    HttpRequestForm form = new HttpRequestForm();
    String uri = httpRequest.uri();
    String host = httpRequest.requestProto().getHost();
    String url = (uri.indexOf("/") == 0 ? host : "") + uri;
    if (url.indexOf("http://") != 0 && url.indexOf("https://") != 0) {
      url = (httpRequest.requestProto().getSsl() ? "https://" : "http://") + url;
    }
    HttpHeadsInfo httpHeadsInfo = (HttpHeadsInfo) httpRequest.headers();
    form.setMethod(httpRequest.method().name());
    form.setUrl(url);
    form.setHeads(httpHeadsInfo.toMap());
    if (httpRequest.content() != null) {
      form.setBody(new String(httpRequest.content()));
    }
    return form;
  }

  @Override
  public String toString() {
    return "HttpRequestForm{" +
        "method='" + method + '\'' +
        ", url='" + url + '\'' +
        ", heads=" + heads +
        ", body='" + body + '\'' +
        '}';
  }
}
