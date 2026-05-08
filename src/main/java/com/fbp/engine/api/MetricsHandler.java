package com.fbp.engine.api;

import com.fbp.engine.api.response.FlowMetricsResponse;
import com.fbp.engine.api.response.NodeStatsResponse;
import com.fbp.engine.metrics.FlowMetrics;
import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.metrics.NodeMetrics;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GET /flows/{id}/metrics : 플로우 메트릭 조회
 * GET /nodes/{id}/status : 노드 상세 통계
 */
@RequiredArgsConstructor
public class MetricsHandler implements HttpHandler {

    private final MetricsCollector metricsCollector;
    private String path;

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equals("GET")) {
            ApiResponse.error(exchange,405, "Method Not Allowed");
            return;
        }

        path = exchange.getRequestURI().getPath(); // 요청 경로 읽기

        if(path.matches("/nodes/[^/]+/status")) { // [^/] : /를 만나기 전까지의 문자열
            getNodeStats(exchange);
            return;
        }

        if(path.matches("/flows/[^/]+/metrics")) {
            getFlowMetrics(exchange);
            return;
        }

        ApiResponse.send(exchange, 404, "Invalid Metrics Path");
    }

    /**
     * GET /nodes/{id}/stats
     * 노드 상세 통계
     * 응답 : {processed, errors, avgTime, queueSize}
     */
    private void getNodeStats(HttpExchange exchange) throws IOException {
        String[] parts = path.split("/");

        String nodeId = parts[2];

        NodeMetrics metrics = metricsCollector.getNodeMetrics(nodeId);

        if(metrics == null) {
            ApiResponse.error(exchange, 404, "Node Not Found");
            return;
        }

        ApiResponse.send(exchange, 200, new NodeStatsResponse(
                metrics.getProcessed(),
                metrics.getErrors(),
                metrics.getAverageTime(),
                metrics.getQueueSize() // TODO queueSize가 뭔데 (일단 0으로 처리)
        ));
    }

    /**
     * GET /flows/{id}/metrics
     * 플로우 메트릭 조회
     * 응답 : {nodes:[{id, processed, errors, avgTime}]}
     */
    private void getFlowMetrics(HttpExchange exchange) throws IOException {
        String[] parts = path.split("/");

        String flowId = parts[2];

        FlowMetrics flowMetrics = metricsCollector.getFlowMetrics(flowId);
        if (flowMetrics == null) {
            ApiResponse.error(exchange, 404, "Flow Not Found");
            return;
        }

        List<FlowMetricsResponse.NodeResponse> nodes = new ArrayList<>();

        for (Map.Entry<String, NodeMetrics> entry : flowMetrics.getNodeMetrics().entrySet()) {
            String nodeId = entry.getKey();
            NodeMetrics metrics = entry.getValue();

            FlowMetricsResponse.NodeResponse nodeResponse =
                    new FlowMetricsResponse.NodeResponse(
                            nodeId,
                            metrics.getProcessed(),
                            metrics.getErrors(),
                            metrics.getAverageTime()
                    );

            nodes.add(nodeResponse);
        }

        ApiResponse.send(exchange, 200, new FlowMetricsResponse(nodes));
    }
}
