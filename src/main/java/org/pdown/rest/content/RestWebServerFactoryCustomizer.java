package org.pdown.rest.content;

import java.io.File;
import org.pdown.rest.entity.ServerConfigInfo;
import org.pdown.rest.util.PathUtil;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class RestWebServerFactoryCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

  public static void init(String... args){
    String baseDir = args != null && args.length > 0 ? args[0] : null;
    String rootPath = PathUtil.ROOT_PATH;
    if (baseDir != null) {
      File dir = new File(baseDir);
      if (!dir.exists() || dir.isFile()) {
        dir.mkdir();
      }
      ServerConfigInfo.baseDir = dir.getPath();
    } else {
      ServerConfigInfo.baseDir = rootPath;
    }
    //server config
    ConfigContent.getInstance().load();
    //download content
    HttpDownContent.getInstance().load();
  }


  @Override
  public void customize(ConfigurableServletWebServerFactory factory) {
    factory.setPort(ConfigContent.getInstance().get().getPort());
  }
}
