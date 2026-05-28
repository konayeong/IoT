package com.fbp.engine.api.response;

import com.fbp.engine.core.Flow;

public record FlowListResponse (
        String id,
        String name,
        String status
) {
    public static FlowListResponse from(Flow flow) {
        return new FlowListResponse(
                flow.getId(),
                flow.getName(),
                flow.getFlowState().name()
        );
    }
}
