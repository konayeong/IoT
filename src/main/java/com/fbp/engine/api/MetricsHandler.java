package com.fbp.engine.api;

import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.metrics.NodeMetrics;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

/**
 * GET /flows/{id}/metrics : 플로우 메트릭 조회
 * GET /nodes/{id}/status : 노드 상세 통계
 */
public class MetricsHandler implements HttpHandler {

    private final MetricsCollector metricsCollector;

    public MetricsHandler(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equals("GET")) {
            ApiResponse.send(exchange,
                    405,
                    """
                    {"message":"Method Not Allowed"}
                    """);
            return;
        }

        String path = exchange.getRequestURI().getPath(); // 요청 경로 읽기

        String[] parts = path.split("/");

        if (parts.length < 4) {
            ApiResponse.send(exchange,
                    400,
                    """
                    {"message":"Invalid Path"}
                    """);

            return;
        }

        String nodeId = parts[2];
        NodeMetrics metrics =
                metricsCollector.getNodeMetrics(nodeId);

        if (metrics == null) {
            ApiResponse.send(exchange,
                    404,
                    """
                    {"message":"Node Not Found"}
                    """);

            return;
        }

        String response = """
                {
                  "processed": %d,
                  "errors": %d,
                  "avgTime": %d
                }
                """.formatted(
                metrics.getProcessed(),
                metrics.getErrors(),
                metrics.getAverageTime()
        );

        ApiResponse.send(exchange, 200, response);
    }
}
