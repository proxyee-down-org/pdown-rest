package org.pdown.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.boot.HttpDownBootstrapBuilder;
import org.pdown.core.constant.HttpDownStatus;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.entity.HttpRequestInfo;
import org.pdown.core.util.HttpDownUtil;
import org.pdown.rest.base.exception.NotFoundException;
import org.pdown.rest.base.exception.ParameterException;
import org.pdown.rest.content.ConfigContent;
import org.pdown.rest.content.HttpDownContent;
import org.pdown.rest.entity.HttpResult;
import org.pdown.rest.entity.ServerConfigInfo;
import org.pdown.rest.form.CreateTaskForm;
import org.pdown.rest.form.HttpRequestForm;
import org.pdown.rest.form.TaskForm;
import org.pdown.rest.vo.ResumeVo;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

  /*
  Create a download task
   */
  @PostMapping("tasks")
  public ResponseEntity create(HttpServletRequest request) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    CreateTaskForm createTaskForm = mapper.readValue(request.getInputStream(), CreateTaskForm.class);
    if (createTaskForm.getRequest() == null) {
      throw new ParameterException(4001, "request can' be empty");
    }
    if (StringUtils.isEmpty(createTaskForm.getRequest().getUrl())) {
      throw new ParameterException(4002, "url can'content be empty");
    }
    HttpDownBootstrapBuilder bootstrapBuilder;
    //if know response Content-Length and file name,can create a task directly, without spending a request to resolve the task name and size.
    if (createTaskForm.getResponse() != null
        && createTaskForm.getResponse().getTotalSize() > 0
        && !StringUtil.isNullOrEmpty(createTaskForm.getResponse().getFileName())) {
      HttpRequestInfo httpRequestInfo = HttpDownUtil.buildGetRequest(createTaskForm.getRequest().getUrl(), createTaskForm.getRequest().getHeads(), createTaskForm.getRequest().getBody());
      bootstrapBuilder = HttpDownBootstrap.builder().request(httpRequestInfo);
    } else {
      bootstrapBuilder = HttpDownBootstrap.builder(createTaskForm.getRequest().getUrl(), createTaskForm.getRequest().getHeads(), createTaskForm.getRequest().getBody());
    }
    HttpDownBootstrap httpDownBootstrap = bootstrapBuilder.response(createTaskForm.getResponse())
        .downConfig(buildConfig(createTaskForm.getConfig()))
        .callback(new PersistenceHttpDownCallback())
        .build();
    HttpDownContent downContent = HttpDownContent.getInstance();
    String id = UUID.randomUUID().toString();
    synchronized (downContent) {
      long runningCount = downContent.get().values().stream()
          .filter(bootstrap -> bootstrap.getTaskInfo().getStatus() == HttpDownStatus.RUNNING)
          .count();
      if (runningCount < ConfigContent.getInstance().get().getTaskLimit()) {
        httpDownBootstrap.start();
      }
    }
    downContent.put(id, httpDownBootstrap).save();
    return ResponseEntity.ok(id);
  }

  @PutMapping("tasks/{id}/pause")
  public ResponseEntity pauseDown(@PathVariable String id) {
    HttpDownBootstrap bootstrap = HttpDownContent.getInstance().get(id);
    if (bootstrap == null) {
      throw new NotFoundException("task does not exist");
    }
    HttpDownContent.getInstance().get(id).pause();
    return ResponseEntity.ok(null);
  }

  @PutMapping("tasks/pause")
  public ResponseEntity<HttpResult> pauseAll() {
    HttpDownContent.getInstance()
        .get()
        .values()
        .stream()
        .filter(httpDownBootstrap -> httpDownBootstrap.getTaskInfo().getStatus() == HttpDownStatus.RUNNING)
        .forEach(httpDownBootstrap -> httpDownBootstrap.pause());
    return ResponseEntity.ok(null);
  }

  @PutMapping("tasks/{id}/resume")
  public ResponseEntity resume(@PathVariable String id) {
    ResumeVo resumeVo = new ResumeVo();
    HttpDownBootstrap bootstrap = HttpDownContent.getInstance().get(id);
    if (bootstrap == null) {
      throw new NotFoundException("task does not exist");
    }
    resumeVo.setResumeIds(Arrays.asList(new String[]{id}));
    if (HttpDownContent.getInstance().get(id).getTaskInfo().getStatus() == HttpDownStatus.PAUSE) {
      resumeVo.setPauseIds(handleResume(1));
      HttpDownContent.getInstance().get(id).resume();
    }
    return ResponseEntity.ok(null);
  }

  @PutMapping("tasks/resume")
  public ResponseEntity resumeAll() {
    ResumeVo resumeVo = new ResumeVo();
    resumeVo.setPauseIds(handleResume(0));
    int runCount = (int) HttpDownContent.getInstance().get()
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().getTaskInfo().getStatus() == HttpDownStatus.RUNNING)
        .count();
    int needResumeCount = ConfigContent.getInstance().get().getTaskLimit() - runCount;
    if (needResumeCount > 0) {
      List<String> resumeIds = new ArrayList<>();
      HttpDownContent.getInstance().get()
          .entrySet()
          .stream()
          .filter(entry -> entry.getValue().getTaskInfo().getStatus() == HttpDownStatus.PAUSE)
          .sorted(Comparator.comparingLong(entry -> entry.getValue().getTaskInfo().getStartTime()))
          .limit(needResumeCount)
          .forEach(entry -> {
            entry.getValue().resume();
            resumeIds.add(entry.getKey());
          });
      resumeVo.setResumeIds(resumeIds);
    }
    return ResponseEntity.ok(resumeVo);
  }

  @GetMapping("tasks")
  public ResponseEntity list() {
    List<TaskForm> list = HttpDownContent.getInstance().get()
        .entrySet()
        .stream()
        .sorted((e1, e2) -> (int) (e2.getValue().getTaskInfo().getStartTime() - e1.getValue().getTaskInfo().getStartTime()))
        .map(entry -> {
          TaskForm taskForm = new TaskForm();
          taskForm.setId(entry.getKey());
          taskForm.setRequest(HttpRequestForm.parse(entry.getValue().getRequest()));
          taskForm.setConfig(entry.getValue().getDownConfig());
          taskForm.setInfo(entry.getValue().getTaskInfo());
          return taskForm;
        })
        .collect(Collectors.toList());
    return ResponseEntity.ok(list);
  }

  @GetMapping("tasks/{id}")
  public ResponseEntity detail(@PathVariable String id) {
    HttpDownBootstrap bootstrap = HttpDownContent.getInstance().get(id);
    if (bootstrap == null) {
      throw new NotFoundException("task does not exist");
    }
    TaskForm taskForm = new TaskForm();
    taskForm.setId(id);
    taskForm.setRequest(HttpRequestForm.parse(bootstrap.getRequest()));
    taskForm.setConfig(bootstrap.getDownConfig());
    taskForm.setInfo(bootstrap.getTaskInfo());
    return ResponseEntity.ok(taskForm);
  }

  @GetMapping("tasks/progress")
  public ResponseEntity progress() {
    List<TaskForm> list = HttpDownContent.getInstance().get()
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().getTaskInfo().getStatus() == HttpDownStatus.RUNNING)
        .map(entry -> {
          TaskForm taskForm = new TaskForm();
          taskForm.setId(entry.getKey());
          taskForm.setInfo(entry.getValue().getTaskInfo());
          return taskForm;
        })
        .collect(Collectors.toList());
    return ResponseEntity.ok(list);
  }

  //Pause running task
  private List<String> handleResume(int resumeCount) {
    List<String> list = new ArrayList<>();
    List<Entry<String, HttpDownBootstrap>> runList = HttpDownContent.getInstance().get()
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().getTaskInfo().getStatus() == HttpDownStatus.RUNNING)
        .collect(Collectors.toList());
    int needPauseCount = runList.size() + resumeCount - ConfigContent.getInstance().get().getTaskLimit();
    if (needPauseCount > 0) {
      for (int i = 0; i < needPauseCount; i++) {
        Entry<String, HttpDownBootstrap> entry = runList.get(i);
        entry.getValue().pause();
        list.add(entry.getKey());
      }
    }
    return list;
  }

  private HttpDownConfigInfo buildConfig(HttpDownConfigInfo configForm) {
    ServerConfigInfo serverConfigInfo = ConfigContent.getInstance().get();
    if (configForm.getConnections() <= 0) {
      configForm.setConnections(serverConfigInfo.getConnections());
    }
    if (configForm.getTimeout() <= 0) {
      configForm.setTimeout(configForm.getTimeout());
    }
    if (configForm.getRetryCount() <= 0) {
      configForm.setRetryCount(configForm.getRetryCount());
    }
    if (!configForm.isAutoRename()) {
      configForm.setAutoRename(serverConfigInfo.isAutoRename());
    }
    if (configForm.getSpeedLimit() <= 0) {
      configForm.setSpeedLimit(configForm.getSpeedLimit());
    }
    return configForm;
  }
}
