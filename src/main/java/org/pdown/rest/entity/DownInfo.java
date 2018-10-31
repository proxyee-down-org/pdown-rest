package org.pdown.rest.entity;

import java.io.Serializable;
import java.util.Map;
import org.pdown.core.boot.HttpDownBootstrap;

public class DownInfo implements Serializable {

  private static final long serialVersionUID = -3046491457601156630L;
  private String id;
  private HttpDownBootstrap bootstrap;
  private Map<String, Object> data;

  public DownInfo() {
  }

  public DownInfo(String id, HttpDownBootstrap bootstrap, Map<String, Object> data) {
    this.id = id;
    this.bootstrap = bootstrap;
    this.data = data;
  }

  public String getId() {
    return id;
  }

  public DownInfo setId(String id) {
    this.id = id;
    return this;
  }

  public HttpDownBootstrap getBootstrap() {
    return bootstrap;
  }

  public DownInfo setBootstrap(HttpDownBootstrap bootstrap) {
    this.bootstrap = bootstrap;
    return this;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public DownInfo setData(Map<String, Object> data) {
    this.data = data;
    return this;
  }
}
