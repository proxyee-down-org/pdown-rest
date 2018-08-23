package org.pdown.rest.util;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.pdown.core.util.OsUtil;

public class PathUtil {

  public static String ROOT_PATH;

  static {
    URL url = PathUtil.class.getResource("/");
    ROOT_PATH = url.getPath();
    try {
      URLConnection connection = url.openConnection();
      if (connection instanceof JarURLConnection) {
        ROOT_PATH = System.getProperty("user.dir");
      }
    } catch (IOException e) {
    }
    if (OsUtil.isWindows() && ROOT_PATH.matches("^/.*$")) {
      ROOT_PATH = ROOT_PATH.substring(1);
    }
    if (ROOT_PATH.matches("^.*[\\\\/]$")) {
      ROOT_PATH = ROOT_PATH.substring(0, ROOT_PATH.length() - 1);
    }
  }
}
