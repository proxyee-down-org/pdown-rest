package org.pdown.rest.content;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import org.pdown.rest.DownRestServer;
import org.pdown.rest.base.content.PersistenceContent;
import org.pdown.rest.controller.ContentHttpDownCallback;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.rest.entity.ConfigInfo;
import org.pdown.rest.util.ContentUtil;

public class HttpDownContent extends PersistenceContent<Map<String, HttpDownBootstrap>, HttpDownContent> {

  private static final HttpDownContent INSTANCE = new HttpDownContent();

  public static HttpDownContent getInstance() {
    return INSTANCE;
  }

  @Override
  protected TypeReference type() {
    return new TypeReference<Map<String, HttpDownBootstrap>>() {
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
    super.load();
    if (content != null && content.size() > 0) {
      content.values().forEach(httpDownBootstrap -> httpDownBootstrap.setCallback(new ContentHttpDownCallback()));
    }
    return this;
  }
}
