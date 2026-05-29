package com.fbp.engine.core;

import lombok.Getter;

@Getter
public class FlowConnection {

    private final String id;

    private final String sourceNodeId;
    private final String sourcePort;

    private final String targetNodeId;
    private final String targetPort;

    private final Connection connection;

    public FlowConnection(
            String sourceNodeId,
            String sourcePort,
            String targetNodeId,
            String targetPort,
            Connection connection
    ) {
        this.id = sourceNodeId + ":" + sourcePort +
                "->" +
                targetNodeId + ":" + targetPort;

        this.sourceNodeId = sourceNodeId;
        this.sourcePort = sourcePort;

        this.targetNodeId = targetNodeId;
        this.targetPort = targetPort;

        this.connection = connection;
    }
}