package com.fbp.engine.api;

import com.fbp.engine.api.response.EngineConditionResponse;
import com.fbp.engine.engine.FlowManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

// /health 엔드포인트 핸들러 : 엔진 상태
public class HealthHandler implements HttpHandler {

    private final FlowManager flowManager;

    public HealthHandler(FlowManager flowManager) {
        this.flowManager = flowManager;
    }

    /**
     * GET /health
     * 엔진 상태
     * 응답 : {status, uptime, flowCount}
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equals("GET")) {
            ApiResponse.error(exchange, 405, "Method Not Allowed");
            return;
        }

        String status = flowManager.getEngineStatus();
        // uptime : 엔진 실행 시간
        long uptime = System.currentTimeMillis() - flowManager.getEngineStart();

        ApiResponse.send(exchange, 200, new EngineConditionResponse(status, uptime, flowManager.flowSize()));
    }
}