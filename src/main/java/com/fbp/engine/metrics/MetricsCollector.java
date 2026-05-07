package com.fbp.engine.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 노드/플로우 메트릭 수집 및 조회
public class MetricsCollector {

    private final Map<String, NodeMetrics> metricsMap = new ConcurrentHashMap<>();

    public void recordProcessing(String nodeId, long startTime, boolean success) {
        NodeMetrics metrics = metricsMap.computeIfAbsent(nodeId, id -> new NodeMetrics());

        if (success) {
            metrics.recordSuccess(startTime);
        } else {
            metrics.recordError();
        }
    }

    public NodeMetrics getNodeMetrics(String nodeId) {
        return metricsMap.get(nodeId);
    }

    public Map<String, NodeMetrics> getAllMetrics() {
        return metricsMap;
    }

    public void reset(String nodeId) {

        NodeMetrics metrics = metricsMap.get(nodeId);

        if (metrics != null) {
            metrics.reset();
        }
    }

    public void resetAll() {
        metricsMap.values().forEach(NodeMetrics::reset);
    }
}