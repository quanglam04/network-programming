package main.com.vn.proxy_server.handler;


import java.io.*;
import java.net.Socket;
import main.com.vn.config.ProxyConfig;
import main.com.vn.util.LoggerUtil;

public class ClientHandler implements Runnable {
  private final Socket client;
  private final ProxyConfig config;

  public ClientHandler(Socket client, ProxyConfig config) {
    this.client = client;
    this.config = config;
  }

  @Override
  public void run() {
    String clientAddr = client.getRemoteSocketAddress().toString();
    try (
        InputStream clientIn = client.getInputStream();
        OutputStream clientOut = client.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientIn))
    ) {
      String requestLine = reader.readLine();
      if (requestLine == null || requestLine.isEmpty()) return;

      if (requestLine.startsWith("CONNECT")) {
        HttpsTunnelHandler.handle(requestLine, clientIn, clientOut, client, clientAddr);
      } else {
        HttpConnector.forward(requestLine, reader, clientIn, clientOut, clientAddr);
      }

    } catch (Exception e) {
      LoggerUtil.error(clientAddr, "Exception: " + e.getMessage());
    }
  }
}