package com.fbp.engine.metrics;

import java.util.concurrent.atomic.AtomicLong;

// 노드 단위 메트릭: 처리량 / 에러 / 시간 집계
public class NodeMetrics {

    private final String nodeId;
    private final AtomicLong processed = new AtomicLong(); // 전체 처리 수 (성공 + 실패)
    private final AtomicLong errors = new AtomicLong();
    private final AtomicLong totalTimeNs = new AtomicLong();

    public NodeMetrics(String nodeId) {
        this.nodeId = nodeId;
    }

    public void recordSuccess(long timeNs) {
        processed.incrementAndGet();
        totalTimeNs.addAndGet(timeNs);
    }

    public void recordFailure(long timeNs) {
        errors.incrementAndGet();
        processed.incrementAndGet();
        totalTimeNs.addAndGet(timeNs);
    }

    public void reset() {
        processed.set(0);
        errors.set(0);
        totalTimeNs.set(0);
    }

    public long getProcessed() {
        return processed.get();
    }

    public long getErrors() {
        return errors.get();
    }

    public double getAvgTimeMs() {
        long count = processed.get();

        if (count == 0) return 0;

        // ✔ ns → ms 변환 후 평균
        return (totalTimeNs.get() / 1_000_000.0) / count;
    }
}