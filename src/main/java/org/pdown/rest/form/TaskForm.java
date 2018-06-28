package org.pdown.rest.form;

import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.entity.TaskInfo;

public class TaskForm {

  private String id;
  private HttpRequestForm request;
  private HttpDownConfigInfo config;
  private TaskInfo info;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public HttpRequestForm getRequest() {
    return request;
  }

  public void setRequest(HttpRequestForm request) {
    this.request = request;
  }

  public HttpDownConfigInfo getConfig() {
    return config;
  }

  public void setConfig(HttpDownConfigInfo config) {
    this.config = config;
  }

  public TaskInfo getInfo() {
    return info;
  }

  public void setInfo(TaskInfo info) {
    this.info = info;
  }
}
