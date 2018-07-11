package org.pdown.rest.base.content;

import com.fasterxml.jackson.core.type.TypeReference;
import org.pdown.rest.util.ContentUtil;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PersistenceContent<E, T extends PersistenceContent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceContent.class);

  protected E content;

  protected abstract TypeReference type();

  protected abstract String savePath();

  protected boolean isHidden() {
    return false;
  }

  protected abstract E defaultValue();

  protected String[] ignoreFields() {
    return null;
  }

  public T save() {
    synchronized (content) {
      try {
        ContentUtil.save(content, savePath(), isHidden(), ignoreFields());
      } catch (IOException e) {
        log("save error", e);
      }
    }
    return (T) this;
  }

  public E get() {
    return content;
  }

  public T load() {
    try {
      content = ContentUtil.get(savePath(), type());
    } catch (IOException e) {
      log("load error", e);
    }
    if (content == null) {
      content = defaultValue();
    }
    return (T) this;
  }

  protected void log(String msg, Exception e) {
    LOGGER.error(msg, e);
  }
}
