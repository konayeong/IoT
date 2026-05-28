package com.fbp.engine.parser;

import java.util.Map;

// 노드 정의
public record NodeDefinition(
        String id,
        String type,
        Map<String, Object> config
) {

    public NodeDefinition {

        if (id == null || id.isBlank()) {
            throw new FlowParserException("Node ID 필수 입력");
        }

        if (type == null || type.isBlank()) {
            throw new FlowParserException("Node type 필수 입력");
        }

        config = (config == null) ? Map.of() : Map.copyOf(config);
    }
}