package main.com.vn.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ProxyMetrics {
  private static final AtomicInteger totalRequests = new AtomicInteger(0);
  private static final CopyOnWriteArrayList<String> logs = new CopyOnWriteArrayList<>();
  private static final int MAX_LOGS = 50; // Tăng số lượng logs lưu trữ

  public static void incrementRequests() {
    totalRequests.incrementAndGet();
  }

  public static int getTotalRequests() {
    return totalRequests.get();
  }

  public static void addLog(String log) {
    // Giới hạn số lượng logs để tránh memory leak
    if (logs.size() >= MAX_LOGS) {
      logs.remove(0); // Remove oldest log
    }
    logs.add(log);
  }

  public static String getLogsAsHtml() {
    StringBuilder sb = new StringBuilder();
    // Reverse order để hiển thị log mới nhất trên đầu
    for (int i = logs.size() - 1; i >= 0; i--) {
      String log = logs.get(i);
      String cssClass = "log-entry";

      // Thêm CSS class based on log type
      if (log.contains("ERROR") || log.contains("502 Bad Gateway")) {
        cssClass += " error";
      } else if (log.contains("CONNECT")) {
        cssClass += " connect";
      }

      sb.append("<li class=\"").append(cssClass).append("\">").append(escapeHtml(log)).append("</li>");
    }
    return sb.toString();
  }

  public static String getLogsAsJson() {
    // Convert logs to JSON array format
    return logs.stream()
        .map(log -> "\"" + escapeJson(log) + "\"")
        .collect(Collectors.joining(","));
  }

  // Get logs in reverse order (newest first)
  public static String[] getLogsArray() {
    String[] result = new String[logs.size()];
    for (int i = 0; i < logs.size(); i++) {
      result[i] = logs.get(logs.size() - 1 - i); // Reverse order
    }
    return result;
  }

  // Utility method để escape HTML characters
  private static String escapeHtml(String text) {
    if (text == null) return "";
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  // Utility method để escape JSON characters
  private static String escapeJson(String text) {
    if (text == null) return "";
    return text.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  // Reset metrics (useful for testing)
  public static void reset() {
    totalRequests.set(0);
    logs.clear();
  }

  // Get statistics summary
  public static String getStatsSummary() {
    return String.format("Total Requests: %d, Active Logs: %d",
        totalRequests.get(), logs.size());
  }

  // Method để lấy logs theo filter
  public static long getErrorCount() {
    return logs.stream()
        .filter(log -> log.contains("ERROR") || log.contains("502 Bad Gateway"))
        .count();
  }

  public static long getConnectCount() {
    return logs.stream()
        .filter(log -> log.contains("CONNECT"))
        .count();
  }

  // Method để debug
  public static void printStats() {
    System.out.println("=== Proxy Metrics ===");
    System.out.println("Total Requests: " + totalRequests.get());
    System.out.println("Total Logs: " + logs.size());
    System.out.println("Error Requests: " + getErrorCount());
    System.out.println("Connect Requests: " + getConnectCount());
    System.out.println("Recent Logs:");
    String[] recentLogs = getLogsArray();
    for (int i = 0; i < Math.min(5, recentLogs.length); i++) {
      System.out.println("  " + recentLogs[i]);
    }
    System.out.println("====================");
  }

  public static String getLogsAsJsonArray() {
    StringBuilder sb = new StringBuilder("[");
    synchronized (logs) {
      for (int i = 0; i < logs.size(); i++) {
        sb.append("\"").append(escapeJson(logs.get(i))).append("\"");
        if (i < logs.size() - 1) sb.append(",");
      }
    }
    sb.append("]");
    return sb.toString();
  }

}