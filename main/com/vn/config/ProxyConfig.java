package main.com.vn.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyConfig {
  private final int port;
  private final ExecutorService executor;

  public ProxyConfig(int port, int maxThreads) {
    this.port = port;
    this.executor = Executors.newFixedThreadPool(maxThreads);
  }

  public int getPort() {
    return port;
  }

  public ExecutorService getExecutor() {
    return executor;
  }
}
