package com.fbp.engine.metrics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeMetricsTest {

    @Test
    @DisplayName("초기값 - 생성 직후 모든 카운터 0")
    void init() {
        NodeMetrics nodeMetrics = new NodeMetrics();
        assertEquals(0, nodeMetrics.getProcessed());
        assertEquals(0, nodeMetrics.getErrors());
        assertEquals(0, nodeMetrics.getTotalProcessingTime());
    }

    @Test
    @DisplayName("increment")
    void increment_process_error() {
        NodeMetrics nodeMetrics = new NodeMetrics();

        // 처리1 - 성공
        nodeMetrics.recordProcess();
        nodeMetrics.recordSuccess(100);

        // 처리2 - 실패
        nodeMetrics.recordProcess();
        nodeMetrics.recordError(200);

        assertEquals(2, nodeMetrics.getProcessed());
        assertEquals(1, nodeMetrics.getErrors());
    }

    @Test
    @DisplayName("평균 계산")
    void averageTime() {
        NodeMetrics nodeMetrics = new NodeMetrics();
        // 처리1 - 성공
        nodeMetrics.recordProcess();
        nodeMetrics.recordSuccess(100);

        // 처리2 - 실패
        nodeMetrics.recordProcess();
        nodeMetrics.recordError(200);

        assertEquals(150, nodeMetrics.getAverageTime());
    }

    @Test
    @DisplayName("스냅샷 - 현재 메트릭의 불변 스냅샷 반환")
    void snapshot() {
        // TODO-Q
    }
}