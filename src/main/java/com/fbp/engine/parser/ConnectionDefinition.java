package com.fbp.engine.parser;

// 연결 정의 (from node:port, to node:port)
public record ConnectionDefinition (
        String fromNode,
        String fromPort,
        String toNode,
        String toPort
){
}