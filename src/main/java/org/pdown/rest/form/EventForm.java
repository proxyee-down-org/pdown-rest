package org.pdown.rest.form;

import org.pdown.rest.websocket.TaskEvent;

public class EventForm {
  private TaskEvent type;
  private Object data;

  public EventForm() {
  }

  public EventForm(TaskEvent type, Object data) {
    this.type = type;
    this.data = data;
  }

  public TaskEvent getType() {
    return type;
  }

  public void setType(TaskEvent type) {
    this.type = type;
  }

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }
}
