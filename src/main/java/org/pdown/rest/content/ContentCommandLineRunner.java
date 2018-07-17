package org.pdown.rest.content;

import java.io.File;
import org.pdown.rest.entity.ServerConfigInfo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ContentCommandLineRunner implements CommandLineRunner {

  @Override
  public void run(String... args) throws Exception {
    String baseDir = args != null && args.length > 0 ? args[0] : null;
    String rootPath = System.getProperty("user.dir");
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
}
