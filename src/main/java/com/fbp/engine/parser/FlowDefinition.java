package com.fbp.engine.parser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record FlowDefinition(
        String id,
        String name,
        String description,

        // 추가
        TransportDefinition transport,

        List<NodeDefinition> nodes,
        List<ConnectionDefinition> connections
) {

    public FlowDefinition {

        if (id == null || id.isBlank()) {
            throw new FlowParserException("Flow ID 필수");
        }

        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        connections = connections == null ? List.of() : List.copyOf(connections);

        if (nodes.isEmpty()) {
            throw new FlowParserException("Flow는 최소 하나 이상의 노드를 포함해야 함");
        }

        validate(nodes, connections);
    }

    private static void validate(
            List<NodeDefinition> nodes,
            List<ConnectionDefinition> connections
    ) {

        Set<String> nodeIds = new HashSet<>();

        // 노드 ID 중복 검사
        for (NodeDefinition node : nodes) {
            if (!nodeIds.add(node.id())) {
                throw new FlowParserException("노드 ID 중복: " + node.id());
            }
        }

        // 연결 검증
        for (ConnectionDefinition conn : connections) {

            if (!nodeIds.contains(conn.fromNode())) {
                throw new FlowParserException(
                        "존재하지 않는 source node: " + conn.fromNode()
                );
            }

            if (!nodeIds.contains(conn.toNode())) {
                throw new FlowParserException(
                        "존재하지 않는 target node: " + conn.toNode()
                );
            }
        }
    }

    public NodeDefinition getNode(String nodeId) {

        return nodes.stream()
                .filter(node -> node.id().equals(nodeId))
                .findFirst()
                .orElse(null);
    }
}