package org.pdown.rest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.pdown.rest.content.RestWebServerFactoryCustomizer;
import org.pdown.rest.util.PathUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DownRestServer {

  static {
    System.setProperty("ROOT_PATH", PathUtil.ROOT_PATH);
  }

  public static void start(String baseDir) throws Exception {
    String[] args = baseDir == null ? new String[0] : new String[]{baseDir};
    RestWebServerFactoryCustomizer.init(args);
    SpringApplication.run(DownRestServer.class, args);
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
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}