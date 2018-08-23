package org.pdown.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.StringUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.boot.HttpDownBootstrapBuilder;
import org.pdown.core.constant.HttpDownStatus;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.entity.HttpRequestInfo;
import org.pdown.core.entity.TaskInfo;
import org.pdown.core.exception.BootstrapCreateDirException;
import org.pdown.core.exception.BootstrapException;
import org.pdown.core.exception.BootstrapFileAlreadyExistsException;
import org.pdown.core.exception.BootstrapNoPermissionException;
import org.pdown.core.exception.BootstrapNoSpaceException;
import org.pdown.core.exception.BootstrapPathEmptyException;
import org.pdown.core.util.FileUtil;
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
import org.pdown.rest.util.ContentUtil;
import org.pdown.rest.vo.ResumeVo;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
      throw new ParameterException(4001, "request can't be empty");
    }
    if (StringUtils.isEmpty(createTaskForm.getRequest().getUrl())) {
      throw new ParameterException(4002, "URL can't be empty");
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
    //build a default taskInfo with WAIT status
    TaskInfo taskInfo = new TaskInfo()
        .setStatus(HttpDownStatus.WAIT)
        .setStartTime(System.currentTimeMillis());
    HttpDownBootstrap httpDownBootstrap = bootstrapBuilder.response(createTaskForm.getResponse())
        .downConfig(buildConfig(createTaskForm.getConfig()))
        .taskInfo(taskInfo)
        .callback(new PersistenceHttpDownCallback())
        .build();
    HttpDownContent downContent = HttpDownContent.getInstance();
    String id = UUID.randomUUID().toString();
    synchronized (downContent) {
      long runningCount = downContent.get().values().stream()
          .filter(bootstrap -> bootstrap.getTaskInfo().getStatus() == HttpDownStatus.RUNNING)
          .count();
      if (runningCount < ConfigContent.getInstance().get().getTaskLimit()) {
        try {
          httpDownBootstrap.start();
        } catch (BootstrapException e) {
          if (e instanceof BootstrapPathEmptyException) {
            throw new ParameterException(4003, "Save path is empty");
          } else if (e instanceof BootstrapCreateDirException) {
            throw new ParameterException(4004, "Can't create dir");
          } else if (e instanceof BootstrapNoPermissionException) {
            throw new ParameterException(4005, "No permission");
          } else if (e instanceof BootstrapNoSpaceException) {
            throw new ParameterException(4006, "No space");
          } else if (e instanceof BootstrapFileAlreadyExistsException) {
            throw new ParameterException(4007, "File already exists");
          }
        }
      }
    }
    downContent.put(id, httpDownBootstrap).save();
    TaskForm taskForm = new TaskForm();
    taskForm.setId(id);
    taskForm.setRequest(HttpRequestForm.parse(httpDownBootstrap.getRequest()));
    taskForm.setConfig(httpDownBootstrap.getDownConfig());
    taskForm.setInfo(httpDownBootstrap.getTaskInfo());
    return ResponseEntity.ok(taskForm);
  }

  @GetMapping("tasks")
  public ResponseEntity list() {
    List<TaskForm> list = HttpDownContent.getInstance().get()
        .entrySet()
        .stream()
        .sorted((e1, e2) -> (int) (e2.getValue().getTaskInfo().getStartTime() - e1.getValue().getTaskInfo().getStartTime()))
        .map(entry -> TaskForm.parse(entry))
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

  @DeleteMapping("tasks/{ids}")
  public ResponseEntity delete(@PathVariable String ids, @RequestParam(required = false) boolean delFile)
      throws IOException {
    HttpDownContent httpDownContent = HttpDownContent.getInstance();
    for (String id : ids.split(",")) {
      HttpDownBootstrap bootstrap = HttpDownContent.getInstance().get(id);
      if (bootstrap == null) {
        continue;
      }
      bootstrap.close();
      //Delete download progress record file
      String recordFile = httpDownContent.progressSavePath(bootstrap.getDownConfig(), bootstrap.getResponse());
      FileUtil.deleteIfExists(recordFile);
      FileUtil.deleteIfExists(ContentUtil.buildBakPath(recordFile));
      if (delFile) {
        //Delete download file
        FileUtil.deleteIfExists(HttpDownUtil.getTaskFilePath(bootstrap));
      }
      httpDownContent.remove(id);
    }
    httpDownContent.save();
    return ResponseEntity.ok(null);
  }

  @DeleteMapping("tasks")
  public ResponseEntity delete(@RequestParam(required = false) boolean delFile)
      throws IOException {
    Set<Entry<String, HttpDownBootstrap>> bootstraps = HttpDownContent.getInstance().get().entrySet();
    for (Entry<String, HttpDownBootstrap> entry : bootstraps) {
      delete(entry.getKey(), delFile);
    }
    return ResponseEntity.ok(null);
  }

  @PutMapping("tasks/{ids}/pause")
  public ResponseEntity pauseDown(@PathVariable String ids) {
    for (String id : ids.split(",")) {
      HttpDownBootstrap httpDownBootstrap = HttpDownContent.getInstance().get(id);
      if (httpDownBootstrap == null) {
        continue;
      }
      HttpDownContent.getInstance().get(id).pause();
    }
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

  @PutMapping("tasks/{ids}/resume")
  public ResponseEntity resume(@PathVariable String ids) {
    return ResponseEntity.ok(handleResume(Arrays.asList(ids.split(","))));
  }

  @PutMapping("tasks/resume")
  public ResponseEntity resumeAll() {
    return ResponseEntity.ok(handleResume(null));
  }

  @GetMapping("tasks/progress")
  public ResponseEntity progress(@RequestParam(required = false) String[] ids) {
    if (ids == null || ids.length == 0) {
      throw new NotFoundException("tasks progress does not exist");
    }
    List<TaskForm> list = Arrays.stream(ids).map(id -> {
      TaskForm taskForm = new TaskForm();
      taskForm.setId(id);
      taskForm.setInfo(HttpDownContent.getInstance().get(id).getTaskInfo());
      return taskForm;
    }).collect(Collectors.toList());
    return ResponseEntity.ok(list);
  }

  //Pause running task
  private ResumeVo handleResume(List<String> resumeIds) {
    ResumeVo resumeVo = new ResumeVo();
    List<Entry<String, HttpDownBootstrap>> runList = new ArrayList<>();
    List<Entry<String, HttpDownBootstrap>> waitList = new ArrayList<>();
    for (Entry<String, HttpDownBootstrap> entry : HttpDownContent.getInstance().get().entrySet()) {
      HttpDownBootstrap httpDownBootstrap = entry.getValue();
      int status = httpDownBootstrap.getTaskInfo().getStatus();
      if (status == HttpDownStatus.RUNNING) {
        runList.add(entry);
      } else if (status != HttpDownStatus.DONE) {
        waitList.add(entry);
      }
    }

    List<String> needResumeIds = new ArrayList<>();
    List<String> needPauseIds = new ArrayList<>();
    int taskLimit = ConfigContent.getInstance().get().getTaskLimit();
    if (resumeIds == null || resumeIds.size() == 0) {
      int needResumeCount = taskLimit - runList.size();
      if (needResumeCount > 0) {
        needResumeIds = waitList.stream()
            .limit(needResumeCount)
            .map(entry -> entry.getKey())
            .collect(Collectors.toList());
      }
    } else {
      needResumeIds = resumeIds.stream()
          .filter(id -> waitList.stream().anyMatch(entry -> id.equals(entry.getKey())))
          .limit(taskLimit)
          .collect(Collectors.toList());
    }
    resumeVo.setResumeIds(needResumeIds);
    resumeVo.setPauseIds(needPauseIds);
    if (needResumeIds.size() > 0) {
      int needPauseCount = runList.size() + needResumeIds.size() - taskLimit;
      if (needPauseCount > 0) {
        for (int i = 0; i < needPauseCount; i++) {
          Entry<String, HttpDownBootstrap> entry = runList.get(i);
          needPauseIds.add(entry.getKey());
          entry.getValue().pause();
        }
      }
      for (Entry<String, HttpDownBootstrap> entry : waitList) {
        if (needResumeIds.contains(entry.getKey())) {
          entry.getValue().resume();
        }
      }
    }
    return resumeVo;
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
