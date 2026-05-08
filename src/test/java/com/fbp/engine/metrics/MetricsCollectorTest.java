package com.fbp.engine.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MetricsCollectorTest {

    private MetricsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new MetricsCollector();
    }

    @Test
    @DisplayName("[처리건수] recordProcessing 호출 후 증가")
    void recordProcessing_success_processCnt() {
        collector.recordProcessing("node-1", 100, true);

        NodeMetrics metrics = collector.getNodeMetrics("node-1");

        assertEquals(1, metrics.getProcessed());
        assertEquals(0, metrics.getErrors());
    }

    @Test
    @DisplayName("에러 건수")
    void recordProcessing_error() {
        collector.recordProcessing("node-1", 100, false);

        NodeMetrics metrics = collector.getNodeMetrics("node-1");

        assertEquals(1, metrics.getProcessed());
        assertEquals(1, metrics.getErrors());
    }

    @Test
    @DisplayName("평균 처리 시간")
    void averageTime() {
        collector.recordProcessing("node-1", 100, true);
        collector.recordProcessing("node-1", 200, false);
        collector.recordProcessing("node-1", 300, true);

        NodeMetrics metrics = collector.getNodeMetrics("node-1");

        assertEquals(200, metrics.getAverageTime());
    }

    @Test
    @DisplayName("멀티스레드 안전성")
    void multiThread() throws InterruptedException {
        MetricsCollector collector = new MetricsCollector();

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    collector.recordProcessing("node-1", 10, true);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        NodeMetrics metrics = collector.getNodeMetrics("node-1");

        assertEquals(10_000, metrics.getProcessed());
    }

    @Test
    @DisplayName("노드별 분리")
    void nodeSeparate() {
        collector.recordProcessing("node-1", 100, true);
        collector.recordProcessing("node-2", 200, false);
        collector.recordProcessing("node-2", 200, true);

        NodeMetrics metrics1 = collector.getNodeMetrics("node-1");
        NodeMetrics metrics2 = collector.getNodeMetrics("node-2");

        assertEquals(1, metrics1.getProcessed());
        assertEquals(2, metrics2.getProcessed());
    }

    @Test
    @DisplayName("리셋")
    void reset() {
        collector.recordProcessing("node-1", 100, true);
        collector.recordProcessing("node-1", 200, false);

        NodeMetrics metrics = collector.getNodeMetrics("node-1");
        assertEquals(2, metrics.getProcessed());

        collector.reset("node-1");
        assertEquals(0, metrics.getProcessed());
    }

    @Test
    @DisplayName("존재하지 않는 노드 - null 반환")
    void notExistsNode() {
        NodeMetrics metrics = collector.getNodeMetrics("not");
        assertNull(metrics);
    }
}
