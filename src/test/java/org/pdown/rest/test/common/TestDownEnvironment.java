package org.pdown.rest.test.common;

import java.io.IOException;
import org.pdown.core.util.FileUtil;
import org.pdown.core.util.OsUtil;
import org.pdown.rest.content.ConfigContent;
import org.pdown.rest.content.HttpDownContent;
import org.pdown.rest.test.server.RangeDownTestServer;
import org.pdown.rest.test.util.TestUtil;

public class TestDownEnvironment {

  private static final String TEST_DIR = System.getProperty("user.dir") + "/target/test";

  private int port;

  public void start() throws Exception {
    clean();
    FileUtil.createDirSmart(TEST_DIR);
    //build random file
    TestUtil.buildRandomFile(getBuildFilePath(), 1024 * 1024 * 500L);
    this.port = OsUtil.getFreePort(8866);
    new RangeDownTestServer(getBuildFilePath()).start(port);

    //org.pdown.rest.test.server config
    ConfigContent.getInstance().load();
    //download content
    HttpDownContent.getInstance().load();
  }

  public void clean() throws IOException {
    String baseDir = System.getProperty("user.dir");
    //delete record
    FileUtil.deleteIfExists(baseDir + "/.records.inf");
    FileUtil.deleteIfExists(baseDir + "/.records.inf.bak");
    FileUtil.deleteIfExists(TEST_DIR);
  }

  public int getPort() {
    return port;
  }

  public String getTestDir() {
    return TEST_DIR;
  }

  public String getBuildFilePath() {
    return TEST_DIR + "/build.data";
  }

  public String getDownFilePath() {
    return TEST_DIR + "/" + getDownFileName();
  }

  public String getDownFileName() {
    return "down.data";
  }
}
