package com.fbp.engine.parser.definition;

import com.fbp.engine.parser.TransportType;

// flow json -> transport 섹션을 안전하게 받아낼 dto
public record TransportDefinition (
        TransportType type,
        String brokerUri,
        Integer qos
) {
}