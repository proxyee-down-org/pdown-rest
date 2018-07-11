package org.pdown.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.pdown.core.constant.HttpDownStatus;
import org.pdown.rest.content.ConfigContent;
import org.pdown.rest.content.HttpDownContent;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.dispatch.HttpDownCallback;
import org.pdown.core.entity.ChunkInfo;
import org.pdown.rest.entity.ServerConfigInfo;

public class PersistenceHttpDownCallback extends HttpDownCallback {

  @Override
  public void onStart(HttpDownBootstrap httpDownBootstrap) {
    calcSpeedLimit();
  }

  @Override
  public void onPause(HttpDownBootstrap httpDownBootstrap) {
    calcSpeedLimit();
    HttpDownContent.getInstance().save();
  }

  @Override
  public void onResume(HttpDownBootstrap httpDownBootstrap) {
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
  }

  @Override
  public void onDone(HttpDownBootstrap httpDownBootstrap) {
    calcSpeedLimit();
    HttpDownContent.getInstance().save();
  }

  private void calcSpeedLimit() {
    ServerConfigInfo serverConfigInfo = ConfigContent.getInstance().get();
    long totalSpeedLimit = serverConfigInfo.getTotalSpeedLimit();
    if (totalSpeedLimit >= 0) {
      List<HttpDownBootstrap> runList = HttpDownContent.getInstance().get()
          .values()
          .stream()
          .filter(bootstrap -> HttpDownStatus.RUNNING == bootstrap.getTaskInfo().getStatus())
          .collect(Collectors.toList());
      if (runList.size() > 0) {
        long avgSpeedLimit = (long) (totalSpeedLimit / (double) runList.size());
        for (HttpDownBootstrap bootstrap : runList) {
          bootstrap.getDownConfig().setSpeedLimit(avgSpeedLimit);
        }
      }
    }
  }
}
