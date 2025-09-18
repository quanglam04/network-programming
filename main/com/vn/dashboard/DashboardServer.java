package main.com.vn.dashboard;


import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import main.com.vn.constant.HttpMethod;
import main.com.vn.util.ProxyMetrics;

public class DashboardServer {
  private final int port;
  private HttpServer server;

  public DashboardServer(int port) {
    this.port = port;
  }

  public void start() throws IOException {
    server = HttpServer.create(new InetSocketAddress(port), 0);

    // Serve dashboard.html
    server.createContext("/", exchange -> {
      exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      if (HttpMethod.GET.equals(exchange.getRequestMethod())) {
        serveDashboard(exchange);
      } else {
        sendResponse(exchange, 405, "Method Not Allowed");
      }
    });

    // API stats
    server.createContext("/api/stats", exchange -> {
      exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      if (HttpMethod.GET.equals(exchange.getRequestMethod())) {
        serveStats(exchange);
      } else {
        sendResponse(exchange, 405, "Method Not Allowed");
      }
    });

    //test
    server.createContext("/api/block/domain", exchange -> {
      exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
        // Lấy InputStream từ request body
        InputStream requestBody = exchange.getRequestBody();

        // Chuyển InputStream sang String
        String body = new BufferedReader(new InputStreamReader(requestBody))
            .lines()
            .collect(Collectors.joining("\n"));

        System.out.println("Data from client: " + body);

        // Gửi response lại client
        String response = "Received!";
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
      } else {
        exchange.sendResponseHeaders(405, -1); // Method Not Allowed
      }
    });

    server.setExecutor(null); // single-threaded
    server.start();
    System.out.println("Dashboard running at http://localhost:" + port);
  }

  private void serveDashboard(HttpExchange exchange) throws IOException {
    String filePath = "C:\\Users\\Lenovo\\Documents\\network-programming\\com.example\\src\\main\\java\\resources\\dashboard.html"; // đường dẫn tới file
    File file = new File(filePath);
    if (!file.exists()) {
      sendResponse(exchange, 404, "dashboard.html not found");
      return;
    }
    byte[] bytes = Files.readAllBytes(Paths.get(filePath));
    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
    exchange.sendResponseHeaders(200, bytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(bytes);
    }
  }

  private void serveStats(HttpExchange exchange) throws IOException {
    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    String json = "{ \"totalRequests\": " + ProxyMetrics.getTotalRequests()
        + ", \"logs\": " + ProxyMetrics.getLogsAsJsonArray() + " }";

    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
    byte[] bytes = json.getBytes();
    exchange.sendResponseHeaders(200, bytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(bytes);
    }
  }

  private void sendResponse(HttpExchange exchange, int status, String message) throws IOException {
    byte[] bytes = message.getBytes();
    exchange.sendResponseHeaders(status, bytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(bytes);
    }
  }

  public void stop() {
    if (server != null) {
      server.stop(0);
      System.out.println("Dashboard stopped");
    }
  }
}
