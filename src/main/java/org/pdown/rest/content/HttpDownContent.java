package org.pdown.rest.content;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.pdown.core.constant.HttpDownStatus;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.entity.HttpRequestInfo;
import org.pdown.core.entity.HttpResponseInfo;
import org.pdown.core.entity.TaskInfo;
import org.pdown.core.util.HttpDownUtil;
import org.pdown.rest.DownRestServer;
import org.pdown.rest.base.content.PersistenceContent;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.rest.controller.PersistenceHttpDownCallback;
import org.pdown.rest.form.HttpRequestForm;
import org.pdown.rest.form.TaskForm;
import org.pdown.rest.util.ContentUtil;

public class HttpDownContent extends PersistenceContent<Map<String, HttpDownBootstrap>, HttpDownContent> {

  private static final HttpDownContent INSTANCE = new HttpDownContent();

  public static HttpDownContent getInstance() {
    return INSTANCE;
  }

  @Override
  protected TypeReference type() {
    return new TypeReference<List<TaskForm>>() {
    };
  }

  @Override
  protected String savePath() {
    return DownRestServer.baseDir + File.separator + ".records.inf";
  }

  @Override
  protected boolean isHidden() {
    return true;
  }

  @Override
  protected Map<String, HttpDownBootstrap> defaultValue() {
    return new LinkedHashMap<>();
  }

  public HttpDownContent put(String id, HttpDownBootstrap httpDownBootstrap) {
    content.put(id, httpDownBootstrap);
    return this;
  }

  public HttpDownBootstrap get(String id) {
    return content.get(id);
  }

  public HttpDownContent remove(String id) {
    content.remove(id);
    return this;
  }

  @Override
  public HttpDownContent load() {
    try {
      List<TaskForm> taskForms = ContentUtil.get(savePath(), type());
      if (taskForms != null && taskForms.size() > 0) {
        content = defaultValue();
        for (int i = 0; i < taskForms.size(); i++) {
          TaskForm taskForm = taskForms.get(i);
          HttpRequestInfo request = HttpDownUtil.buildGetRequest(taskForm.getRequest().getUrl(), taskForm.getRequest().getHeads(), taskForm.getRequest().getBody());
          TaskInfo taskInfo = null;
          try {
            taskInfo = ContentUtil.get(progressSavePath(taskForm.getConfig(), taskForm.getResponse()), TaskInfo.class);
          } catch (Exception e) {
          }
          if (taskInfo != null && taskInfo.getStatus() != HttpDownStatus.DONE) {
            taskInfo.setStatus(HttpDownStatus.PAUSE);
          }
          HttpDownBootstrap httpDownBootstrap = HttpDownBootstrap.builder()
              .request(request)
              .response(taskForm.getResponse())
              .downConfig(taskForm.getConfig())
              .taskInfo(taskInfo)
              .callback(new PersistenceHttpDownCallback())
              .build();
          content.put(taskForm.getId(), httpDownBootstrap);
        }
      }
    } catch (Exception e) {
      log("load error", e);
    }
    if (content == null) {
      content = defaultValue();
    }
    return this;
  }

  @Override
  public HttpDownContent save() {
    if (content.size() > 0) {
      List<TaskForm> taskForms = content.entrySet().stream().map(entry -> {
        TaskForm taskForm = new TaskForm();
        taskForm.setId(entry.getKey());
        taskForm.setRequest(HttpRequestForm.parse(entry.getValue().getRequest()));
        taskForm.setResponse(entry.getValue().getResponse());
        taskForm.setConfig(entry.getValue().getDownConfig());
        return taskForm;
      }).collect(Collectors.toList());
      try {
        synchronized (content) {
          ContentUtil.save(taskForms, savePath(), isHidden());
        }
      } catch (IOException e) {
        log("save error", e);
      }
    }
    return this;
  }

  private String progressSavePath(HttpDownConfigInfo config, HttpResponseInfo response) {
    return config.getFilePath() + File.separator + "." + response.getFileName() + ".inf";
  }

  public HttpDownContent save(HttpDownBootstrap bootstrap) {
    if (bootstrap != null) {
      TaskInfo taskInfo = bootstrap.getTaskInfo();
      if (taskInfo != null) {
        try {
          synchronized (bootstrap) {
            ContentUtil.save(taskInfo, progressSavePath(bootstrap.getDownConfig(), bootstrap.getResponse()), isHidden());
          }
        } catch (IOException e) {
          log("save progress error", e);
        }
      }
    }
    return this;
  }
}
