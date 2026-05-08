package com.fbp.engine.api;

import com.fbp.engine.api.response.FlowDeployResponse;
import com.fbp.engine.api.response.FlowListResponse;
import com.fbp.engine.core.Flow;
import com.fbp.engine.engine.FlowManager;
import com.fbp.engine.parser.definition.FlowDefinition;
import com.fbp.engine.parser.FlowParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// /flows 엔드포인트 핸들러
@RequiredArgsConstructor
public class FlowHandler implements HttpHandler {

    private final FlowManager flowManager;
    private final FlowParser flowParser;
    private final MetricsHandler metricsHandler;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // metrics는 MetricsHandler로
        if(method.equals("GET") && path.matches("/flows/[^/]+/metrics")) {
            metricsHandler.handle(exchange);
            return;
        }

        if(path.equals("/flows")) {
            handleCollection(exchange, method);
            return;
        }

        if(path.matches("/flows/[^/]+")) {
            handleItem(exchange, method, path);
            return;
        }

        ApiResponse.error(exchange, 404, "Not Found");
    }

    private void handleCollection(HttpExchange exchange, String method) throws IOException {
        switch (method) {
            case "GET" -> getFlows(exchange);
            case "POST" -> createFlow(exchange);
            default -> ApiResponse.error(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleItem(HttpExchange exchange, String method, String path) throws IOException {
        String flowId = path.split("/")[2];

        switch (method) {
            case "DELETE" -> deleteFlow(exchange, flowId);
            default -> ApiResponse.error(exchange, 405, "Method Not Allowed");
        }
    }

    /**
     * GET /flows
     * 실행 중인 플로우 목록
     * 응답 : [{id, name, status}]
     */
    private void getFlows(HttpExchange exchange) throws IOException {
        List<FlowListResponse> flowList = new ArrayList<>();

        Map<String, Flow> runningFlow = flowManager.getRunningFlows();
        for(Flow flow : runningFlow.values()) {
            flowList.add(FlowListResponse.from(flow));
        }

        ApiResponse.send(exchange, 200, flowList);
    }

    /**
     * POST /flows
     * 새 플로우 배포
     * 요청 본문 : 플로우 정의 JSON
     * 응답 : {id, status} TODO-Q flow status를 말하는건가?
     */
    private void createFlow(HttpExchange exchange) throws IOException {
        FlowDefinition definition = flowParser.parse(exchange.getRequestBody());

        Flow flow = flowManager.deploy(definition);

        ApiResponse.send(exchange, 201, FlowDeployResponse.from(flow));
    }

    /**
     * DELETE /flows/{id}
     * 플로우 중지 및 삭제
     * 응답 : {message}
     */
    private void deleteFlow(HttpExchange exchange, String flowId) throws IOException {
        boolean removed = flowManager.remove(flowId);

        if (!removed) {
            ApiResponse.error(exchange, 404, "Flow Not Found");
            return;
        }

        ApiResponse.send(exchange, 200, Map.of("message", "Flow Deleted"));
    }


}
