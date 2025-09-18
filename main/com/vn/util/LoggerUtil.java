package main.com.vn.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

  public static void info(String msg) {
    System.out.println("[INFO " + LocalDateTime.now().format(TIME_FORMAT) + "] " + msg);
  }

  public static void error(String msg) {
    System.err.println("[ERROR " + LocalDateTime.now().format(TIME_FORMAT) + "] " + msg);
  }

  public static void error(String clientAddr, String msg) {
    System.err.println("[ERROR " + LocalDateTime.now().format(TIME_FORMAT) + "] [" + clientAddr + "] " + msg);
  }
}
