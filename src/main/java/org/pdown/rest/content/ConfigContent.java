package org.pdown.rest.content;

import com.fasterxml.jackson.core.type.TypeReference;
import org.pdown.rest.DownRestServer;
import org.pdown.rest.base.content.PersistenceContent;
import org.pdown.rest.entity.ServerConfigInfo;
import java.io.File;

public class ConfigContent extends PersistenceContent<ServerConfigInfo, ConfigContent> {

  private static final ConfigContent INSTANCE = new ConfigContent();

  public static ConfigContent getInstance() {
    return INSTANCE;
  }

  @Override
  protected TypeReference type() {
    return new TypeReference<ServerConfigInfo>() {
    };
  }

  @Override
  protected String savePath() {
    return DownRestServer.baseDir + File.separator + "config.inf";
  }

  @Override
  protected ServerConfigInfo defaultValue() {
    return new ServerConfigInfo();
  }
}
