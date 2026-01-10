package io.github.axpx.algotrading.metrics;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class MetricsServer {

    private static final Logger logger = LoggerFactory.getLogger(MetricsServer.class);
    private static final int PORT = 9090;

    public static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/metrics", exchange -> {
            String response = MetricsRegistry.scrape();
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.setExecutor(null);
        server.start();

        logger.info("Metrics server started on http://localhost:{}/metrics", PORT);
    }

}
