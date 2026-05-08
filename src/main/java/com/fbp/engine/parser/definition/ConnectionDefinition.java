package com.fbp.engine.parser.definition;

import com.fbp.engine.parser.FlowParserException;
import lombok.Getter;

// 연결 정의 (from node:port, to node:port)
@Getter
public class ConnectionDefinition {
    private final String fromNode;
    private final String fromPort;
    private final String toNode;
    private final String toPort;

    public ConnectionDefinition(String from, String to) {
        String[] fromParts = split(from);
        String[] toParts = split(to);

        this.fromNode = fromParts[0];
        this.fromPort = fromParts[1];
        this.toNode = toParts[0];
        this.toPort = toParts[1];
    }


    private String[] split(String value) {
        if (value == null || !value.contains(":")) {
            throw new FlowParserException("Invalid connection format: " + value);
        }

        String[] parts = value.split(":");
        if (parts.length != 2) {
            throw new FlowParserException("Invalid connection format: " + value);
        }

        return parts;
    }
}
