package com.fbp.engine.api;

import com.fbp.engine.api.response.NodeStatsResponse;
import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.metrics.NodeMetrics;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.RequiredArgsConstructor;
import java.io.IOException;

@RequiredArgsConstructor
public class MetricsHandler implements HttpHandler {

    private final MetricsCollector metricsCollector;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (!method.equals("GET")) {
            ApiResponse.error(exchange, 405, "Method Not Allowed");
            return;
        }

        // GET /nodes/{id}/status
        if (path.matches("/nodes/[^/]+/status")) {
            getNodeStats(exchange, path);
            return;
        }

        ApiResponse.error(exchange, 404, "Invalid Metrics Path");
    }

    private void getNodeStats(HttpExchange exchange, String path) throws IOException {
        String nodeId = path.split("/")[2];

        NodeMetrics metrics = metricsCollector.getNodeMetrics(nodeId);

        if (metrics == null) {
            ApiResponse.error(exchange, 404, "Node Not Found");
            return;
        }

        ApiResponse.send(exchange, 200, new NodeStatsResponse(
                metrics.getProcessed(),
                metrics.getErrors(),
                metrics.getAvgTimeMs(),
                0
        ));
    }
}