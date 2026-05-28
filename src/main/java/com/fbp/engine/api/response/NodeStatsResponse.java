package com.fbp.engine.api.response;

public record NodeStatsResponse (
    long processed,
    long errors,
    double avgTime,
    long queueSize
) {
}
