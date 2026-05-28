package com.fbp.engine.api.response;

import java.util.List;

public record FlowMetricsResponse (
        List<NodeResponse> nodes
){
    public record NodeResponse(
            String id,
            long processed,
            long errors,
            long avgTime
    ) {
    }
}
