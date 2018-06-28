package org.pdown.rest;

import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.pdown.rest.content.ConfigContent;
import org.pdown.rest.content.HttpDownContent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DownRestServer {

  public static String baseDir;

  //init configuration
  public static void init(String baseDir) {
    String rootPath = System.getProperty("user.dir");
    //设置slf4j日志打印目录
    System.setProperty("LOG_PATH", rootPath);
    if (baseDir == null) {
      baseDir = rootPath;
    }
    File dir = new File(baseDir);
    if (!dir.exists() || dir.isFile()) {
      dir.mkdir();
    }
    DownRestServer.baseDir = dir.getPath();
    //org.pdown.rest.test.server config
    ConfigContent.getInstance().load();
    //download content
    HttpDownContent.getInstance().load();
  }

  public static void start(String baseDir) {
    init(baseDir);
    SpringApplication.run(DownRestServer.class, new String[0]);
  }

  public static void main(String[] args) {
    Options options = new Options();
    options.addOption("h", "help", false, "See the help.");
    options.addOption("b", "baseDir", false, "The basic path of the org.pdown.rest.test.server operation");
    String tips = "pdServer [-b/--baseDir][-h/--help]";
    HelpFormatter formatter = new HelpFormatter();
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cl = parser.parse(options, args);
      if (cl.hasOption("h")) {
        formatter.printHelp(tips, options);
        return;
      }
      start(cl.getOptionValue("b"));
    } catch (ParseException e) {
      formatter.printHelp("Unrecognized option", options);
    }
  }

}