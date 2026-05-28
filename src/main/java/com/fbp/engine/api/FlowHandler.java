package com.fbp.engine.api;

import com.fbp.engine.api.response.FlowDeployResponse;
import com.fbp.engine.api.response.FlowListResponse;
import com.fbp.engine.core.Flow;
import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.parser.FlowDefinition;
import com.fbp.engine.parser.FlowManager;
import com.fbp.engine.parser.FlowParser;
import com.fbp.engine.parser.FlowParserException;
import com.sun.jdi.request.DuplicateRequestException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

// /flows 엔드포인트 핸들러
@RequiredArgsConstructor
public class FlowHandler implements HttpHandler {

    private final FlowManager flowManager;
    private final FlowParser flowParser;
    private final MetricsCollector metricsCollector;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();


            // 플로우 메트릭 조회 : {nodes: [{id, processed, errors, avgTime}]}
            if (method.equals("GET") && path.matches("/flows/[^/]+/metrics")) {
                handleFlowMetrics(exchange, path);
                return;
            }

            if (path.equals("/flows")) {
                handleCollection(exchange, method);
                return;
            }

            if (path.matches("/flows/[^/]+")) {
                handleItem(exchange, method, path);
                return;
            }

            ApiResponse.error(exchange, 404, "Not Found");
        }catch (Exception e) {
            ApiResponse.error(exchange, 500, e.getMessage());
        }

    }

    private void handleCollection(HttpExchange exchange, String method) throws IOException {
        switch (method) {

            // 실행 중인 플로우 목록 : [{id, name, status}]
            case "GET" -> {
                List<FlowListResponse> flows =
                        flowManager.getRunningFlows()
                                .stream()
                                .map(FlowListResponse::from)
                                .toList();

                ApiResponse.ok(exchange, flows);
            }

            // 새 플로우 배포 : {id, status}
            case "POST" -> {
                FlowDefinition definition;
                Flow flow = null;

                try {
                    definition = flowParser.parse(exchange.getRequestBody());
                    flow = flowManager.deploy(definition);
                }catch (FlowParserException e) {
                    ApiResponse.error(exchange, 400, "Bad Request");
                }catch (DuplicateRequestException e) {
                    ApiResponse.error(exchange, 409, "Conflict");
                }


                ApiResponse.created(exchange, FlowDeployResponse.from(flow));
            }

            default -> ApiResponse.error(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleItem(HttpExchange exchange, String method, String path) throws IOException {
        String flowId = extractFlowId(path);

        switch (method) {
            // 플로우 중지 및 삭제 : {message}
            case "DELETE" -> {
                boolean removed = false;
                try {
                    removed = flowManager.remove(flowId);
                }catch (IllegalArgumentException e) {
                    ApiResponse.error(exchange, 404, "Flow Not Found");
                }

                if (!removed) {
                    ApiResponse.error(exchange, 404, "Flow Not Found");
                    return;
                }

                ApiResponse.ok(exchange, Map.of("message", "Flow Deleted"));
            }

            default -> ApiResponse.error(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleFlowMetrics(HttpExchange exchange, String path) throws IOException {
        String flowId = extractFlowId(path);

        try {
            flowManager.getFlow(flowId);
        }catch (FlowNotFoundException e) {
            ApiResponse.error(exchange, 404, "NotFound");
        }
        ApiResponse.ok(exchange, metricsCollector.getFlowMetrics(flowId));
    }

    private String extractFlowId(String path) {
        String[] parts = path.split("/");
        return parts[2]; // /flows/{id}
    }
}