package com.fbp.engine.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

class MetricsCollectorTest {

    private MetricsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new MetricsCollector();
    }

    @Test
    @DisplayName("처리 건수 기록")
    void record_success_count() {

        collector.recordSuccess("node-1", 1000);

        NodeMetrics metrics = collector.getNodeMetrics("node-1");

        assertNotNull(metrics);
        assertEquals(1, metrics.getProcessed());
        assertEquals(0, metrics.getErrors());
    }

    @Test
    @DisplayName("에러 건수 기록")
    void record_failure_count() {

        collector.recordFailure("node-1", 1000);

        NodeMetrics metrics = collector.getNodeMetrics("node-1");

        assertEquals(1, metrics.getErrors());
        assertEquals(1, metrics.getProcessed());
    }

    @Test
    @DisplayName("평균 처리 시간")
    void average_time() {

        collector.recordSuccess("node-1", 1_000_000); // 1ms
        collector.recordSuccess("node-1", 2_000_000); // 2ms

        NodeMetrics metrics = collector.getNodeMetrics("node-1");

        assertEquals(1.5, metrics.getAvgTimeMs(), 0.0001);
    }

    @Test
    @DisplayName("멀티스레드 안전성")
    void concurrent_recording_should_be_thread_safe() throws InterruptedException {

        int threads = 10;
        int iterations = 1000;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                for (int i = 0; i < iterations; i++) {
                    collector.recordSuccess("node-1", 1000);
                }
                latch.countDown();
            });
        }

        latch.await();

        NodeMetrics metrics = collector.getNodeMetrics("node-1");

        assertEquals(threads * iterations, metrics.getProcessed());

        executor.shutdown();
    }

    @Test
    @DisplayName("노드별 분리")
    void metrics_should_be_separated_by_node() {

        collector.recordSuccess("node-A", 1000);
        collector.recordSuccess("node-B", 1000);
        collector.recordFailure("node-B", 1000);

        NodeMetrics a = collector.getNodeMetrics("node-A");
        NodeMetrics b = collector.getNodeMetrics("node-B");

        assertEquals(1, a.getProcessed());
        assertEquals(0, a.getErrors());

        assertEquals(2, b.getProcessed());
        assertEquals(1, b.getErrors());
    }

    @Test
    @DisplayName("리셋")
    void reset() {

        collector.recordSuccess("node-1", 1000);
        collector.recordFailure("node-1", 1000);

        collector.reset("node-1");

        NodeMetrics metrics = collector.getNodeMetrics("node-1");

        assertEquals(0, metrics.getProcessed());
        assertEquals(0, metrics.getErrors());
    }

    @Test
    @DisplayName("존재하지 않는 노드")
    void unknown_node() {

        NodeMetrics metrics = collector.getNodeMetrics("unknown-node");

        assertNull(metrics);
    }
}