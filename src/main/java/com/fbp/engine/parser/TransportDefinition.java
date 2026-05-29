package com.fbp.engine.parser;


// flow json -> transport 섹션을 안전하게 받아낼 dto
public record TransportDefinition (
        TransportType type,
        String brokerUri,
        Integer qos
) {
}