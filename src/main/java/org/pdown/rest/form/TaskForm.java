package org.pdown.rest.form;

import java.util.Map.Entry;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.entity.HttpResponseInfo;
import org.pdown.core.entity.TaskInfo;

public class TaskForm {

  private String id;
  private HttpRequestForm request;
  private HttpResponseInfo response;
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

  public static TaskForm parse(Entry<String,HttpDownBootstrap> entry){
    TaskForm taskForm = new TaskForm();
    taskForm.setId(entry.getKey());
    taskForm.setRequest(HttpRequestForm.parse(entry.getValue().getRequest()));
    taskForm.setResponse(entry.getValue().getResponse());
    taskForm.setConfig(entry.getValue().getDownConfig());
    taskForm.setInfo(entry.getValue().getTaskInfo());
    return taskForm;
  }
}
