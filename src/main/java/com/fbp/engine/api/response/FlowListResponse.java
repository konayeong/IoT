package com.fbp.engine.api.response;

public record FlowListResponse (
        String id,
        String name,
        String status // TODO-Q String vs enum
) {
}
