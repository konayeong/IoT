package com.fbp.engine.api;

import com.fbp.engine.api.response.EngineConditionResponse;
import com.fbp.engine.parser.FlowManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

// /health 엔드포인트 : 엔진 상태
public class HealthHandler implements HttpHandler {

    private final FlowManager flowManager;

    public HealthHandler(FlowManager flowManager) {
        this.flowManager = flowManager;
    }

     // health : {status, uptime, flowCount}
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equals("GET")) {
            ApiResponse.error(exchange, 405, "Method Not Allowed");
            return;
        }

        String status = flowManager.getEngineStatus();

        // ✔ uptime 계산 (ms)
        long uptime = System.currentTimeMillis() - flowManager.getEngineStart();

        int flowCount = flowManager.flowSize();

        ApiResponse.send(exchange, 200, new EngineConditionResponse(status, uptime, flowCount));
    }
}