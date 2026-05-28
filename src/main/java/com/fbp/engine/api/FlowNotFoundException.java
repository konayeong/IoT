package com.fbp.engine.api;

public class FlowNotFoundException extends RuntimeException {
    public FlowNotFoundException(String id) {
        super("존재하지 않는 FlowId: " + id);
    }
}
