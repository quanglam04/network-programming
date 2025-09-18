package main.com.vn.proxy_server.handler;

import java.net.URI;

public class HttpRequestParser {
  public static String extractHostFromRequestLine(String requestLine) {
    String[] parts = requestLine.split(" ");
    if (parts.length < 2) return null;
    String uri = parts[1];
    try {
      if (uri.startsWith("http://") || uri.startsWith("https://")) {
        URI u = new URI(uri);
        return u.getHost() + (u.getPort() == -1 ? "" : ":" + u.getPort());
      }
    } catch (Exception ignored) {}
    return null;
  }

  public static String rewriteRequestLine(String requestLine) {
    String[] parts = requestLine.split(" ", 3);
    if (parts.length < 3) return requestLine;
    String method = parts[0], uri = parts[1], version = parts[2];
    try {
      if (uri.startsWith("http://") || uri.startsWith("https://")) {
        URI u = new URI(uri);
        String path = u.getRawPath();
        if (path == null || path.isEmpty()) path = "/";
        if (u.getRawQuery() != null) path += "?" + u.getRawQuery();
        return method + " " + path + " " + version;
      }
    } catch (Exception ignored) {}
    return requestLine;
  }
}