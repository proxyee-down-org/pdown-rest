package org.pdown.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.nio.NioEventLoopGroup;
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
  @PostMapping("resolve")
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
  @PostMapping("create")
  public ResponseEntity<HttpResult> create(HttpServletRequest request) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    CreateTaskForm createTaskForm = mapper.readValue(request.getInputStream(), CreateTaskForm.class);
    commonCheck(createTaskForm);
    String taskId = commonCreate(HttpDownBootstrap.builder(createTaskForm.getRequest().getUrl(), createTaskForm.getRequest().getHeads(), createTaskForm.getRequest().getBody())
        .response(createTaskForm.getResponse())
        .downConfig(createTaskForm.getConfig()));
    return ResponseEntity.ok().body(new HttpResult().data(taskId));
  }

  /*
  Create a download task, if know response Content-Length and file name,can create a task directly, without spending a request to resolve the task name and size.
   */
  @PostMapping("createDirect")
  public ResponseEntity<HttpResult> createDirect(HttpServletRequest request) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    CreateTaskForm createTaskForm = mapper.readValue(request.getInputStream(), CreateTaskForm.class);
    commonCheck(createTaskForm);
    HttpRequestInfo httpRequestInfo = HttpDownUtil.buildGetRequest(createTaskForm.getRequest().getUrl(), createTaskForm.getRequest().getHeads(), createTaskForm.getRequest().getBody());
    String taskId = commonCreate(HttpDownBootstrap.builder()
        .request(httpRequestInfo)
        .response(createTaskForm.getResponse())
        .downConfig(createTaskForm.getConfig()));
    return ResponseEntity.ok().body(new HttpResult().data(taskId));
  }

  @PutMapping("pause/{id}")
  public ResponseEntity<HttpResult> pauseDown(@PathVariable String id) {
    HttpDownBootstrap bootstrap = HttpDownContent.getInstance().get(id);
    if (bootstrap == null) {
      throw new NotFoundException("task does not exist");
    }
    HttpDownContent.getInstance().get(id).pauseDown();
    return ResponseEntity.ok().body(new HttpResult().msg("OK"));
  }

  @PutMapping("continue/{id}")
  public ResponseEntity<HttpResult> continueDown(@PathVariable String id) {
    HttpDownBootstrap bootstrap = HttpDownContent.getInstance().get(id);
    if (bootstrap == null) {
      throw new NotFoundException("task does not exist");
    }
    HttpDownContent.getInstance().get(id).continueDown();
    return ResponseEntity.ok().body(new HttpResult().msg("OK"));
  }


  @GetMapping("list")
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

  @GetMapping("progress")
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

  private void commonCheck(CreateTaskForm createTaskForm) {
    if (createTaskForm.getRequest() == null) {
      throw new ParameterException("request can' be empty");
    }
    if (StringUtils.isEmpty(createTaskForm.getRequest().getUrl())) {
      throw new ParameterException("url can'content be empty");
    }
  }

  private String commonCreate(HttpDownBootstrapBuilder builder) {
    ConfigContent configContent = ConfigContent.getInstance();
    HttpDownContent downContent = HttpDownContent.getInstance();
    HttpDownBootstrap httpDownBootstrap = builder.proxyConfig(configContent.get().getProxyConfig())
        .callback(new ContentHttpDownCallback())
        .proxyConfig(ConfigContent.getInstance().get().getProxyConfig())
        .build();
    String id = UUID.randomUUID().toString();
    synchronized (downContent) {
      long runningCount = downContent.get().values().stream()
          .filter(bootstrap -> bootstrap.getTaskInfo().getStatus() == HttpDownStatus.RUNNING)
          .count();
      if (runningCount < ConfigContent.getInstance().get().getTaskLimit()) {
        httpDownBootstrap.startDown();
      }
    }
    downContent.put(id, httpDownBootstrap).save();
    return id;
  }
}
