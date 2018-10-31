package org.pdown.rest.form;

import java.util.Map;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.entity.HttpResponseInfo;
import org.pdown.core.entity.TaskInfo;
import org.pdown.rest.entity.DownInfo;

public class TaskForm {

  private String id;
  private HttpRequestForm request;
  private HttpResponseInfo response;
  private HttpDownConfigInfo config;
  private TaskInfo info;
  private Map<String, Object> data;

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

  public TaskInfo getInfo() {
    return info;
  }

  public void setInfo(TaskInfo info) {
    this.info = info;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public TaskForm setData(Map<String, Object> data) {
    this.data = data;
    return this;
  }

  public static TaskForm parse(DownInfo downInfo) {
    TaskForm taskForm = new TaskForm();
    HttpDownBootstrap bootstrap = downInfo.getBootstrap();
    taskForm.setId(downInfo.getId());
    taskForm.setRequest(HttpRequestForm.parse(bootstrap.getRequest()));
    taskForm.setResponse(bootstrap.getResponse());
    taskForm.setConfig(bootstrap.getDownConfig());
    taskForm.setInfo(bootstrap.getTaskInfo());
    taskForm.setData(downInfo.getData());
    return taskForm;
  }
}
