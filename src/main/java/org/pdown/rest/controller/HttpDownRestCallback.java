package org.pdown.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.constant.HttpDownStatus;
import org.pdown.core.dispatch.HttpDownCallback;
import org.pdown.core.proxy.ProxyConfig;
import org.pdown.rest.content.ConfigContent;
import org.pdown.rest.content.HttpDownContent;
import org.pdown.rest.entity.DownInfo;
import org.pdown.rest.entity.ServerConfigInfo;
import org.pdown.rest.form.EventForm;
import org.pdown.rest.form.TaskForm;
import org.pdown.rest.vo.ResumeVo;
import org.pdown.rest.websocket.TaskEvent;
import org.pdown.rest.websocket.TaskEventHandler;

public class HttpDownRestCallback extends HttpDownCallback {

  private static HttpDownRestCallback callback;

  public synchronized static HttpDownRestCallback getCallback() {
    if (callback == null) {
      callback = new HttpDownRestCallback();
    }
    return callback;
  }

  public synchronized static void setCallback(HttpDownRestCallback callback) {
    if (HttpDownRestCallback.callback == null) {
      HttpDownRestCallback.callback = callback;
    }
  }

  @Override
  public void onStart(HttpDownBootstrap httpDownBootstrap) {
    calcSpeedLimit();
    commonConfig(httpDownBootstrap);
  }

  @Override
  public void onResume(HttpDownBootstrap httpDownBootstrap) {
    commonConfig(httpDownBootstrap);
  }

  @Override
  public void onError(HttpDownBootstrap httpDownBootstrap) {
    calcSpeedLimit();
    HttpDownContent.getInstance().save();
    HttpDownContent.getInstance().save(httpDownBootstrap);
    String taskId = findTaskId(httpDownBootstrap);
    if (taskId != null) {
      TaskForm taskForm = new TaskForm();
      taskForm.setId(taskId);
      taskForm.setInfo(httpDownBootstrap.getTaskInfo());
      TaskEventHandler.dispatchEvent(new EventForm(TaskEvent.ERROR, taskForm));
    }
  }

  @Override
  public void onProgress(HttpDownBootstrap httpDownBootstrap) {
    HttpDownContent.getInstance().save(httpDownBootstrap);
    String taskId = findTaskId(httpDownBootstrap);
    if (taskId != null) {
      TaskForm taskForm = new TaskForm();
      taskForm.setId(taskId);
      taskForm.setInfo(httpDownBootstrap.getTaskInfo());
      TaskEventHandler.dispatchEvent(new EventForm(TaskEvent.PROGRESS, taskForm));
    }
  }

  @Override
  public void onDone(HttpDownBootstrap httpDownBootstrap) {
    calcSpeedLimit();
    HttpDownContent.getInstance().save();
    //删除进度记录文件
    HttpDownContent.getInstance().remove(httpDownBootstrap);
    //开始待下载的任务
    int taskLimit = ConfigContent.getInstance().get().getTaskLimit();
    int runSize = (int) HttpDownContent.getInstance().get().values().stream()
        .filter(downInfo -> downInfo.getBootstrap().getTaskInfo().getStatus() == HttpDownStatus.RUNNING)
        .count();
    int resumeSize = taskLimit - runSize;
    if (resumeSize > 0) {
      List<DownInfo> resumeList = HttpDownContent.getInstance().get().values()
          .stream()
          .filter(downInfo -> downInfo.getBootstrap().getTaskInfo().getStatus() == HttpDownStatus.WAIT)
          .limit(resumeSize)
          .collect(Collectors.toList());
      if (resumeList != null && resumeList.size() > 0) {
        resumeList.forEach(downInfo -> downInfo.getBootstrap().resume());
        ResumeVo resumeVo = new ResumeVo();
        resumeVo.setResumeIds(resumeList.stream().map(downInfo -> downInfo.getId()).collect(Collectors.toList()));
        TaskEventHandler.dispatchEvent(new EventForm(TaskEvent.RESUME, resumeVo));
      }
    }
  }

  protected String findTaskId(HttpDownBootstrap httpDownBootstrap) {
    DownInfo sameDown = HttpDownContent.getInstance().get().values()
        .stream()
        .filter(downInfo -> downInfo.getBootstrap() == httpDownBootstrap)
        .findFirst()
        .orElse(null);
    if (sameDown == null) {
      return null;
    }
    return sameDown.getId();
  }

  protected DownInfo findDownInfo(HttpDownBootstrap httpDownBootstrap) {
    String taskId = findTaskId(httpDownBootstrap);
    if (taskId == null) {
      return null;
    }
    return HttpDownContent.getInstance().get(taskId);
  }

  private void commonConfig(HttpDownBootstrap httpDownBootstrap) {
    ServerConfigInfo serverConfigInfo = ConfigContent.getInstance().get();
    long speedLimit = serverConfigInfo.getSpeedLimit();
    ProxyConfig proxyConfig = serverConfigInfo.getProxyConfig();
    if (speedLimit > 0) {
      httpDownBootstrap.getDownConfig().setSpeedLimit(speedLimit);
    }
    if (proxyConfig != null) {
      httpDownBootstrap.setProxyConfig(proxyConfig);
    }
  }

  public static void calcSpeedLimit() {
    ServerConfigInfo serverConfigInfo = ConfigContent.getInstance().get();
    long totalSpeedLimit = serverConfigInfo.getTotalSpeedLimit();
    long speedLimit = serverConfigInfo.getSpeedLimit();
    List<DownInfo> runList = HttpDownContent.getInstance().get()
        .values()
        .stream()
        .filter(downInfo -> HttpDownStatus.RUNNING == downInfo.getBootstrap().getTaskInfo().getStatus())
        .collect(Collectors.toList());
    if (runList.size() > 0) {
      if (totalSpeedLimit > 0) {
        long avgSpeedLimit = (long) (totalSpeedLimit / (double) runList.size());
        for (DownInfo downInfo : runList) {
          downInfo.getBootstrap().getDownConfig().setSpeedLimit(avgSpeedLimit);
          refreshLast(downInfo.getBootstrap());
        }
      } else if (speedLimit > 0) {
        for (DownInfo downInfo : runList) {
          downInfo.getBootstrap().getDownConfig().setSpeedLimit(speedLimit);
          refreshLast(downInfo.getBootstrap());
        }
      } else {
        for (DownInfo downInfo : runList) {
          downInfo.getBootstrap().getDownConfig().setSpeedLimit(0);
        }
      }
    }
  }

  //刷新最后下载时间和最后下载的字节数，用于计算下载速度限制
  private static void refreshLast(HttpDownBootstrap bootstrap) {
    bootstrap.getTaskInfo().setLastStartTime(0);
    bootstrap.getTaskInfo().setLastDownSize(bootstrap.getTaskInfo().getDownSize());
  }
}
