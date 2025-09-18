package main.com.vn.util;

import java.io.*;

import java.net.Socket;

public class StreamUtil {
  public static void pipe(InputStream in, Socket outSocket) {
    try (OutputStream out = outSocket.getOutputStream()) {
      byte[] buf = new byte[8192];
      int len;
      while ((len = in.read(buf)) != -1) {
        out.write(buf, 0, len);
        out.flush();
      }
    } catch (IOException ignored) {}
  }

  public static void pipe(Socket inSocket, OutputStream out) {
    try (InputStream in = inSocket.getInputStream()) {
      byte[] buf = new byte[8192];
      int len;
      while ((len = in.read(buf)) != -1) {
        out.write(buf, 0, len);
        out.flush();
      }
    } catch (IOException ignored) {}
  }

  public static void sendSimpleBadRequest(OutputStream out, String message) throws IOException {
    String resp = "HTTP/1.1 " + message + "\r\nContent-Length: 0\r\nConnection: close\r\n\r\n";
    out.write(resp.getBytes());
  }

  public static byte[] readFixedBytes(InputStream in, int len) throws IOException {
    byte[] data = new byte[len];
    int read = 0;
    while (read < len) {
      int r = in.read(data, read, len - read);
      if (r == -1) throw new EOFException("Unexpected EOF reading body");
      read += r;
    }
    return data;
  }
}