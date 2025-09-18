package main.com.vn.proxy_server.handler;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import main.com.vn.util.ProxyMetrics;
import main.com.vn.util.LoggerUtil;
import main.com.vn.util.StreamUtil;

public class HttpConnector {
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

  public static void forward(String requestLine, BufferedReader clientReader,
      InputStream clientIn, OutputStream clientOut,
      String clientAddr) throws IOException {

    // Read headers
    List<String> headerLines = new ArrayList<>();
    String line;
    while ((line = clientReader.readLine()) != null && !line.isEmpty()) {
      headerLines.add(line);
    }

    Map<String, String> headers = new HashMap<>();
    for (String h : headerLines) {
      int idx = h.indexOf(':');
      if (idx > 0) {
        headers.put(h.substring(0, idx).trim().toLowerCase(), h.substring(idx + 1).trim());
      }
    }

    String hostHeader = headers.get("host");
    if (hostHeader == null) hostHeader = HttpRequestParser.extractHostFromRequestLine(requestLine);
    if (hostHeader == null) {
      StreamUtil.sendSimpleBadRequest(clientOut, "400 Host header missing");
      return;
    }

    String host;
    int port = 80;
    if (hostHeader.contains(":")) {
      String[] hp = hostHeader.split(":");
      host = hp[0];
      port = Integer.parseInt(hp[1]);
    } else {
      host = hostHeader;
    }

    byte[] requestBody = new byte[0];
    if (headers.containsKey("content-length")) {
      int contentLength = Integer.parseInt(headers.get("content-length"));
      requestBody = StreamUtil.readFixedBytes(clientIn, contentLength);
    }

    String forwardRequestLine = HttpRequestParser.rewriteRequestLine(requestLine);
    StringBuilder forwardRequest = new StringBuilder();
    forwardRequest.append(forwardRequestLine).append("\r\n");
    for (String h : headerLines) {
      String name = h.split(":", 2)[0].trim().toLowerCase();
      if (name.equals("proxy-connection") || name.equals("connection")) continue;
      forwardRequest.append(h).append("\r\n");
    }
    forwardRequest.append("Connection: close\r\n\r\n");

    try (Socket serverSocket = new Socket()) {
      serverSocket.connect(new InetSocketAddress(host, port), 5000);
      serverSocket.setSoTimeout(30000);

      OutputStream serverOut = serverSocket.getOutputStream();
      InputStream serverIn = serverSocket.getInputStream();

      serverOut.write(forwardRequest.toString().getBytes());
      if (requestBody.length > 0) serverOut.write(requestBody);
      serverOut.flush();

      byte[] buffer = new byte[8192];
      int read;
      while ((read = serverIn.read(buffer)) != -1) {
        clientOut.write(buffer, 0, read);
      }
      clientOut.flush();

      String logMessage = "[" + LocalDateTime.now().format(TIME_FORMAT) + "] [" + clientAddr + "] " + requestLine + " -> " + host + ":" + port;
      LoggerUtil.info(logMessage);
      ProxyMetrics.incrementRequests();
      ProxyMetrics.addLog(logMessage);

    } catch (IOException ex) {
      StreamUtil.sendSimpleBadRequest(clientOut, "502 Bad Gateway");
      String errorLog = "[" + LocalDateTime.now().format(TIME_FORMAT) + "] [" + clientAddr + "] ERROR: " + requestLine + " -> " + host + ":" + port;
      LoggerUtil.error(errorLog);
      ProxyMetrics.addLog(errorLog);
    }
  }
}
