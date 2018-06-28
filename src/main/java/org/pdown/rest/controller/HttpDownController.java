package org.pdown.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.internal.StringUtil;
import org.pdown.rest.base.exception.NotFoundException;
import org.pdown.rest.base.exception.ParameterException;
import org.pdown.rest.content.ConfigContent;
import org.pdown.rest.content.HttpDownContent;
import org.pdown.rest.entity.HttpResult;
import org.pdown.rest.form.CreateTaskForm;
import org.pdown.rest.form.HttpRequestForm;
import org.pdown.rest.form.TaskForm;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.boot.HttpDownBootstrapBuilder;
import org.pdown.core.constant.HttpDownStatus;
import org.pdown.core.entity.HttpRequestInfo;
import org.pdown.core.entity.HttpResponseInfo;
import org.pdown.core.util.HttpDownUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HttpDownController {

  /*
  Resolve request
   */
  @PostMapping("api/resolve")
  public ResponseEntity<HttpResult> resolve(HttpServletRequest request) throws Exception {
    NioEventLoopGroup loopGroup = null;
    try {
      ObjectMapper mapper = new ObjectMapper();
      HttpRequestForm requestForm = mapper.readValue(request.getInputStream(), HttpRequestForm.class);
      if (StringUtils.isEmpty(requestForm.getUrl())) {
        throw new ParameterException("url can't be empty");
      }
      HttpRequestInfo httpRequestInfo = HttpDownUtil.buildGetRequest(requestForm.getUrl(), requestForm.getHeads(), requestForm.getBody());
      loopGroup = new NioEventLoopGroup(1);
      HttpResponseInfo httpResponseInfo = HttpDownUtil.getHttpResponseInfo(httpRequestInfo, null, null, loopGroup);
      return ResponseEntity.ok().body(new HttpResult().data(httpResponseInfo));
    } finally {
      if (loopGroup != null) {
        loopGroup.shutdownGracefully();
      }
    }
  }

  /*
  Create a download task, join the download queue after requesting parsing task related information.
   */
  @PostMapping("tasks")
  public ResponseEntity<HttpResult> create(HttpServletRequest request) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    CreateTaskForm createTaskForm = mapper.readValue(request.getInputStream(), CreateTaskForm.class);
    if (createTaskForm.getRequest() == null) {
      throw new ParameterException("request can' be empty");
    }
    if (StringUtils.isEmpty(createTaskForm.getRequest().getUrl())) {
      throw new ParameterException("url can'content be empty");
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
        .downConfig(createTaskForm.getConfig())
        .callback(new PersistenceHttpDownCallback())
        .proxyConfig(ConfigContent.getInstance().get().getProxyConfig())
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
    return ResponseEntity.ok().body(new HttpResult().data(id));
  }

  @PutMapping("tasks/{id}/pause")
  public ResponseEntity<HttpResult> pauseDown(@PathVariable String id) {
    HttpDownBootstrap bootstrap = HttpDownContent.getInstance().get(id);
    if (bootstrap == null) {
      throw new NotFoundException("task does not exist");
    }
    HttpDownContent.getInstance().get(id).pause();
    return ResponseEntity.ok().body(new HttpResult().msg("success"));
  }

  @PutMapping("tasks/{id}/resume")
  public ResponseEntity<HttpResult> resume(@PathVariable String id) {
    HttpDownBootstrap bootstrap = HttpDownContent.getInstance().get(id);
    if (bootstrap == null) {
      throw new NotFoundException("task does not exist");
    }
    HttpDownContent.getInstance()
        .get(id)
        .setProxyConfig(ConfigContent.getInstance().get().getProxyConfig())
        .resume();
    return ResponseEntity.ok().body(new HttpResult().msg("success"));
  }


  @GetMapping("tasks")
  public ResponseEntity<HttpResult> list() {
    List<TaskForm> list = HttpDownContent.getInstance().get()
        .entrySet()
        .stream()
        .map(entry -> {
          TaskForm taskForm = new TaskForm();
          taskForm.setId(entry.getKey());
          taskForm.setRequest(HttpRequestForm.parse(entry.getValue().getRequest()));
          taskForm.setConfig(entry.getValue().getDownConfig());
          taskForm.setInfo(entry.getValue().getTaskInfo());
          return taskForm;
        })
        .collect(Collectors.toList());
    return ResponseEntity.ok().body(new HttpResult().data(list));
  }

  @GetMapping("tasks/{id}")
  public ResponseEntity<HttpResult> detail(@PathVariable String id) {
    HttpDownBootstrap bootstrap = HttpDownContent.getInstance().get(id);
    if (bootstrap == null) {
      throw new NotFoundException("task does not exist");
    }
    TaskForm taskForm = new TaskForm();
    taskForm.setId(id);
    taskForm.setRequest(HttpRequestForm.parse(bootstrap.getRequest()));
    taskForm.setConfig(bootstrap.getDownConfig());
    taskForm.setInfo(bootstrap.getTaskInfo());
    return ResponseEntity.ok().body(new HttpResult().data(taskForm));
  }

  @GetMapping("tasks/progress")
  public ResponseEntity<HttpResult> progress() {
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
    return ResponseEntity.ok().body(new HttpResult().data(list));
  }
}
