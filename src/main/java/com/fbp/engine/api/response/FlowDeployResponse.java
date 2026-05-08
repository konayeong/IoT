package com.fbp.engine.api.response;

import com.fbp.engine.core.Flow;

public record FlowDeployResponse (
        String id,
        String status
) {
    public static FlowDeployResponse from(Flow flow) {
        return new FlowDeployResponse(
                flow.getId(),
                flow.getFlowState().name()
        );
    }
}
