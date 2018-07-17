package org.pdown.rest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DownRestServer {

  static {
    System.setProperty("LOG_PATH", System.getProperty("user.dir"));
  }

  public static void start(String baseDir) {
    SpringApplication.run(DownRestServer.class, baseDir == null ? new String[0] : new String[]{baseDir});
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