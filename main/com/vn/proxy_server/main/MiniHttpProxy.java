package main.com.vn.proxy_server.main;


import java.net.ServerSocket;
import java.net.Socket;
import main.com.vn.config.ProxyConfig;
import main.com.vn.dashboard.DashboardServer;
import main.com.vn.proxy_server.handler.ClientHandler;
import main.com.vn.util.LoggerUtil;

public class MiniHttpProxy {
  private final ProxyConfig config;

  public MiniHttpProxy(ProxyConfig config) {
    this.config = config;
  }

  public void start() throws Exception {
    try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
      LoggerUtil.info("Proxy listening on port " + config.getPort());
      while (true) {
        Socket client = serverSocket.accept();
        config.getExecutor().submit(new ClientHandler(client, config));
      }
    }
  }

  public static void main(String[] args) throws Exception {
    int port = 8080;
    int threads = 50;
    int dashboardPort = 9090;
    if (args.length >= 1) port = Integer.parseInt(args[0]);
    if (args.length >= 2) threads = Integer.parseInt(args[1]);

    ProxyConfig config = new ProxyConfig(port, threads);
    DashboardServer dashboard = new DashboardServer(dashboardPort);
    dashboard.start();
    new MiniHttpProxy(config).start();


  }
}