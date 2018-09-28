package org.pdown.rest.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.constant.HttpDownStatus;
import org.pdown.core.dispatch.HttpDownCallback;
import org.pdown.core.entity.ChunkInfo;
import org.pdown.core.proxy.ProxyConfig;
import org.pdown.rest.content.ConfigContent;
import org.pdown.rest.content.HttpDownContent;
import org.pdown.rest.entity.ServerConfigInfo;
import org.pdown.rest.form.EventForm;
import org.pdown.rest.form.TaskForm;
import org.pdown.rest.vo.ResumeVo;
import org.pdown.rest.websocket.TaskEvent;
import org.pdown.rest.websocket.TaskEventHandler;

public class PersistenceHttpDownCallback extends HttpDownCallback {

  @Override
  public void onStart(HttpDownBootstrap httpDownBootstrap) {
    calcSpeedLimit();
    commonConfig(httpDownBootstrap);
  }

  @Override
  public void onPause(HttpDownBootstrap httpDownBootstrap) {
    calcSpeedLimit();
    HttpDownContent.getInstance().save();
  }

  @Override
  public void onResume(HttpDownBootstrap httpDownBootstrap) {
    commonConfig(httpDownBootstrap);
    calcSpeedLimit();
    HttpDownContent.getInstance().save();
  }

  @Override
  public void onError(HttpDownBootstrap httpDownBootstrap) {
    calcSpeedLimit();
    HttpDownContent.getInstance().save();
  }

  @Override
  public void onChunkDone(HttpDownBootstrap httpDownBootstrap, ChunkInfo chunkInfo) {
    HttpDownContent.getInstance().save();
  }

  @Override
  public void onProgress(HttpDownBootstrap httpDownBootstrap) {
    HttpDownContent.getInstance().save(httpDownBootstrap);
    String taskId = findTaskId(httpDownBootstrap);
    if (taskId != null) {
      TaskForm taskForm = new TaskForm();
      taskForm.setId(findTaskId(httpDownBootstrap));
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
    //开始一个待下载的任务
    HttpDownBootstrap waitBootstrap = HttpDownContent.getInstance().get().values()
        .stream()
        .filter(bootstrap -> bootstrap.getTaskInfo().getStatus() == HttpDownStatus.WAIT)
        .findFirst()
        .orElse(null);
    if (waitBootstrap != null) {
      waitBootstrap.resume();
      ResumeVo resumeVo = new ResumeVo();
      resumeVo.setResumeIds(Arrays.asList(findTaskId(waitBootstrap)));
      TaskEventHandler.dispatchEvent(new EventForm(TaskEvent.RESUME, resumeVo));
    }
  }

  private String findTaskId(HttpDownBootstrap httpDownBootstrap) {
    Entry<String, HttpDownBootstrap> entry = HttpDownContent.getInstance().get().entrySet()
        .stream()
        .filter(e -> e.getValue() == httpDownBootstrap)
        .findFirst()
        .orElse(null);
    if (entry == null) {
      return null;
    }
    return entry.getKey();
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
    List<HttpDownBootstrap> runList = HttpDownContent.getInstance().get()
        .values()
        .stream()
        .filter(bootstrap -> HttpDownStatus.RUNNING == bootstrap.getTaskInfo().getStatus())
        .collect(Collectors.toList());
    if (runList.size() > 0) {
      if (totalSpeedLimit > 0) {
        long avgSpeedLimit = (long) (totalSpeedLimit / (double) runList.size());
        for (HttpDownBootstrap bootstrap : runList) {
          bootstrap.getDownConfig().setSpeedLimit(avgSpeedLimit);
          refreshLast(bootstrap);
        }
      } else if (speedLimit > 0) {
        for (HttpDownBootstrap bootstrap : runList) {
          bootstrap.getDownConfig().setSpeedLimit(speedLimit);
          refreshLast(bootstrap);
        }
      } else {
        for (HttpDownBootstrap bootstrap : runList) {
          bootstrap.getDownConfig().setSpeedLimit(0);
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
