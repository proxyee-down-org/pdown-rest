package org.pdown.rest.controller;

import org.pdown.rest.content.HttpDownContent;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.dispatch.HttpDownCallback;
import org.pdown.core.entity.ChunkInfo;

public class PersistenceHttpDownCallback extends HttpDownCallback {

  @Override
  public void onPause(HttpDownBootstrap httpDownBootstrap) {
    HttpDownContent.getInstance().save();
  }

  @Override
  public void onResume(HttpDownBootstrap httpDownBootstrap) {
    HttpDownContent.getInstance().save();
  }

  @Override
  public void onError(HttpDownBootstrap httpDownBootstrap) {
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
    HttpDownContent.getInstance().save();
  }
}
