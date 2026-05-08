package com.fbp.engine.metrics;

import java.util.concurrent.atomic.AtomicLong;

// 노드별 메트릭 데이터 (처리 건수, 에러 수, 평균 처리 시간)
public class NodeMetrics {
    private final AtomicLong processed = new AtomicLong();
    private final AtomicLong errors = new AtomicLong();
    private final AtomicLong totalProcessingTime = new AtomicLong();

    public void recordProcess() {
        processed.incrementAndGet();
    }

    public void recordSuccess(long processingTime) {
        totalProcessingTime.addAndGet(processingTime);
    }

    public void recordError(long processingTime) {
        errors.incrementAndGet();
        totalProcessingTime.addAndGet(processingTime);
    }

    public long getAverageTime() {
        long count = processed.get();

        if (count == 0) {
            return 0;
        }

        return totalProcessingTime.get() / count;
    }

    public void reset() {
        processed.set(0);
        errors.set(0);
        totalProcessingTime.set(0);
    }

    // getter
    public long getProcessed() {
        return processed.get();
    }

    public long getErrors() {
        return errors.get();
    }

    public long getTotalProcessingTime() {
        return totalProcessingTime.get();
    }

    public long getQueueSize() {
        return 0;
    }
}