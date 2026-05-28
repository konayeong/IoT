package com.fbp.engine.api.response;

public record EngineConditionResponse (
        String status,
        long uptime,
        int flowSize
) {}
