package com.fbp.engine.parser.definition;

import lombok.Getter;

@Getter
public class TransportDefinition {
    private final String type;
    private final String broker;
    private final int qos;

    public TransportDefinition(String type, String broker, int qos) {
        // TODO QOS는 ?
        this.type = type == null ? "local" : type;
        this. broker = broker;
        this.qos = qos;
    }
}
