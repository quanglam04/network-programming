package main.com.vn.proxy_server.handler;


import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import main.com.vn.util.ProxyMetrics;
import main.com.vn.util.LoggerUtil;
import main.com.vn.util.StreamUtil;

public class HttpsTunnelHandler {
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

  public static void handle(String requestLine, InputStream clientIn, OutputStream clientOut,
      Socket clientSocket, String clientAddr) throws IOException {
    String[] parts = requestLine.split(" ");
    if (parts.length < 2) return;

    String hostPort = parts[1];
    String host = hostPort;
    int port = 443;
    if (hostPort.contains(":")) {
      String[] hp = hostPort.split(":");
      host = hp[0];
      port = Integer.parseInt(hp[1]);
    }

    try (Socket serverSocket = new Socket(host, port)) {
      clientOut.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
      clientOut.flush();

      Thread t1 = new Thread(() -> StreamUtil.pipe(clientIn, serverSocket));
      Thread t2 = new Thread(() -> StreamUtil.pipe(serverSocket, clientOut));
      t1.start(); t2.start();
      try { t1.join(); t2.join(); } catch (InterruptedException ignored) {}

      String logMessage = "[" + LocalDateTime.now().format(TIME_FORMAT) + "] [" + clientAddr + "] CONNECT " + host + ":" + port;
      LoggerUtil.info(logMessage);
      ProxyMetrics.incrementRequests();
      ProxyMetrics.addLog(logMessage);

    } catch (IOException e) {
      clientOut.write("HTTP/1.1 502 Bad Gateway\r\n\r\n".getBytes());
      String errorLog = "[" + LocalDateTime.now().format(TIME_FORMAT) + "] [" + clientAddr + "] CONNECT ERROR: " + host + ":" + port;
      LoggerUtil.error(errorLog);
      ProxyMetrics.addLog(errorLog);
    }
  }
}
