package com.fbp.engine.metrics;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicLong;

// 노드별 메트릭 데이터 (처리 건수, 에러 수, 평균 처리 시간)
@Getter
public class NodeMetrics {
    private final AtomicLong processed = new AtomicLong();
    private final AtomicLong errors = new AtomicLong();
    private final AtomicLong totalProcessingTime = new AtomicLong();

    public void recordSuccess(long processingTime) {
        processed.incrementAndGet();
        totalProcessingTime.addAndGet(processingTime);
    }

    public void recordError() {
        errors.incrementAndGet();
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
}