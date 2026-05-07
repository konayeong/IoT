package com.fbp.engine.parser;

import lombok.Getter;

import java.util.Map;

// 노드 정의
@Getter
public class NodeDefinition {
    private final String id;
    private final String type;
    private final Map<String, Object> config;

    public NodeDefinition(String id, String type, Map<String, Object> config) {
        if (id == null || id.isBlank()) {
            throw new FlowParserException("Node ID 필수 입력");
        }
        if (type == null || type.isBlank()) {
            throw new FlowParserException("Node type 필수 입력");
        }

        this.id = id;
        this.type = type;
        this.config = config == null ? Map.of() : Map.copyOf(config);
    }
}
