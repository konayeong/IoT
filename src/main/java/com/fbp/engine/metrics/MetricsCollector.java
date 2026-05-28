package com.fbp.engine.metrics;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

// 노드/플로우 메트릭 수집 및 조회
public class MetricsCollector {

    private final Map<String, NodeMetrics> nodeMetricsMap = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> flowNodes = new ConcurrentHashMap<>();

    public void recordSuccess(String nodeId, long processingTimeNs) {
        NodeMetrics metrics = nodeMetricsMap.computeIfAbsent(nodeId, NodeMetrics::new);

        metrics.recordSuccess(processingTimeNs);
    }

    public void recordFailure(String nodeId, long processingTimeNs) {
        NodeMetrics metrics = nodeMetricsMap.computeIfAbsent(nodeId, NodeMetrics::new);

        metrics.recordFailure(processingTimeNs);
    }

    public void registerFlow(String flowId, Set<String> nodeIds) {
        flowNodes.put(flowId, nodeIds);
    }

    public NodeMetrics getNodeMetrics(String nodeId) {
        return nodeMetricsMap.get(nodeId);
    }

    public FlowMetrics getFlowMetrics(String flowId) {
        Set<String> nodeIds = flowNodes.get(flowId);

        if (nodeIds == null) {
            return null;
        }

        Map<String, NodeMetrics> snapshot = new HashMap<>();

        for (String nodeId : nodeIds) {
            NodeMetrics metrics = nodeMetricsMap.get(nodeId);

            if (metrics != null) {
                snapshot.put(nodeId, metrics);
            }
        }

        return new FlowMetrics(flowId, snapshot);
    }

    public void reset(String nodeId) {
        NodeMetrics metrics = nodeMetricsMap.get(nodeId);

        if (metrics != null) {
            metrics.reset();
        }
    }

    public Map<String, NodeMetrics> snapshot() { // 읽기 전용 복사
        return Map.copyOf(nodeMetricsMap);
    }
}