package com.fbp.engine.api;

import com.fbp.engine.engine.FlowManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

// /health 엔드포인트 핸들러
public class HealthHandler implements HttpHandler {

    private final FlowManager flowManager;
    private final long startedAt;

    public HealthHandler(FlowManager flowManager) {
        this.flowManager = flowManager;
        // TODO startedAt의 정체가 뭐지
        this.startedAt = System.currentTimeMillis();
    }

    @Override
    public void handle(HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod().equals("GET")) {
            ApiResponse.send(exchange,
                    405,
                    """
                    {"message":"Method Not Allowed"}
                    """);
            return;
        }

        long uptime = System.currentTimeMillis() - startedAt;

        String response = """
                {
                  "status":"UP",
                  "uptime": %d,
                  "flowCount": %d
                }
                """.formatted(
                uptime,
                flowManager.flowSize()
        );

        ApiResponse.send(exchange, 200, response);
    }
}