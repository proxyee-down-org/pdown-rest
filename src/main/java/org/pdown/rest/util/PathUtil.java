package org.pdown.rest.util;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class PathUtil {

  public static String ROOT_PATH;

  static {
    URL url = PathUtil.class.getResource("/");
    ROOT_PATH = url.getPath();
    try {
      URLConnection connection = url.openConnection();
      if(connection instanceof JarURLConnection){
        ROOT_PATH = System.getProperty("user.dir");
      }
    } catch (IOException e) {
    }
  }
}
