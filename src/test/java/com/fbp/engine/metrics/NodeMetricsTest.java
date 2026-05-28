package com.fbp.engine.metrics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeMetricsTest {

    @Test
    @DisplayName("초기값 - 생성 직후 모든 카운트가 0")
    void initial_values_should_be_zero() {
        NodeMetrics metrics = new NodeMetrics("node-1");

        assertEquals(0, metrics.getProcessed());
        assertEquals(0, metrics.getErrors());
        assertEquals(0, metrics.getAvgTimeMs());
    }

    @Test
    @DisplayName("increment - 처리 건수, 에러 건수 증가")
    void increment_should_work_correctly() {
        NodeMetrics metrics = new NodeMetrics("node-1");

        metrics.recordSuccess(1_000_000);
        metrics.recordFailure(2_000_000);

        assertEquals(2, metrics.getProcessed());
        assertEquals(1, metrics.getErrors());
    }

    @Test
    @DisplayName("평균 계산 - 처리 시간 합계 / 처리 건수 = 평균")
    void average_should_be_calculated_correctly() {
        NodeMetrics metrics = new NodeMetrics("node-1");

        metrics.recordSuccess(1_000_000); // 1ms
        metrics.recordSuccess(3_000_000); // 3ms

        assertEquals(2.0, metrics.getAvgTimeMs(), 0.0001);
    }

    @Test
    @DisplayName("스냅샷 - 현재 메트릭의 불변 스냅샷 반환")
    void reset_should_clear_values() {
        NodeMetrics metrics = new NodeMetrics("node-1");

        metrics.recordSuccess(1_000_000);
        metrics.recordFailure(1_000_000);

        metrics.reset();

        assertEquals(0, metrics.getProcessed());
        assertEquals(0, metrics.getErrors());
        assertEquals(0, metrics.getAvgTimeMs());
    }
}