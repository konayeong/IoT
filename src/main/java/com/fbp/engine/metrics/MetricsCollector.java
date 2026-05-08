package com.fbp.engine.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// 노드/플로우 메트릭 수집 및 조회
public class MetricsCollector {

    private final Map<String, NodeMetrics> nodeMetricsMap = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> flowNodes = new ConcurrentHashMap<>(); // flow가 어떤 node들을 포함하는지 저장

    public void recordProcessing(String nodeId, long processingTime, boolean success) {
        NodeMetrics metrics = nodeMetricsMap.computeIfAbsent(nodeId, id -> new NodeMetrics());

        metrics.recordProcess();

        if (success) {
            metrics.recordSuccess(processingTime);
        } else {
            metrics.recordError(processingTime);
        }
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

        Map<String, NodeMetrics> result = new HashMap<>();

        for (String nodeId : nodeIds) {
            NodeMetrics metrics = nodeMetricsMap.get(nodeId);

            if (metrics != null) {
                result.put(nodeId, metrics);
            }
        }

        return new FlowMetrics(flowId, result);
    }

    public void reset(String nodeId) {

        NodeMetrics metrics = nodeMetricsMap.get(nodeId);

        if (metrics != null) {
            metrics.reset();
        }
    }

    public void resetAll() {
        nodeMetricsMap.values().forEach(NodeMetrics::reset);
    }
}