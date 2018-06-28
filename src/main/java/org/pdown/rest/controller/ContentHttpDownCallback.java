package org.pdown.rest.controller;

import org.pdown.rest.content.HttpDownContent;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.dispatch.HttpDownCallback;
import org.pdown.core.entity.ChunkInfo;

public class ContentHttpDownCallback extends HttpDownCallback {

  @Override
  public void onPause(HttpDownBootstrap httpDownBootstrap) {
    HttpDownContent.getInstance().save();
  }

  @Override
  public void onContinue(HttpDownBootstrap httpDownBootstrap) {
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
  public void onDone(HttpDownBootstrap httpDownBootstrap) {
    HttpDownContent.getInstance().save();
  }
}
