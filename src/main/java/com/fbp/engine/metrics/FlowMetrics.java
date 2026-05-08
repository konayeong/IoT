package com.fbp.engine.metrics;

import lombok.Getter;
import java.util.Map;

// 플로우 전체 메트릭 집계
@Getter
public class FlowMetrics {

    private final String flowId;
    private final Map<String, NodeMetrics> nodeMetrics;

    public FlowMetrics(String flowId,
                       Map<String, NodeMetrics> nodeMetrics) {

        this.flowId = flowId;
        this.nodeMetrics = nodeMetrics;
    }

}