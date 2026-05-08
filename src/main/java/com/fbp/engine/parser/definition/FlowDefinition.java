package com.fbp.engine.parser.definition;

import com.fbp.engine.parser.FlowParserException;
import lombok.Getter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 플로우 정의 데이터 객체 (노드 목록, 연결 목록) - 검증
@Getter
public class FlowDefinition {
    private final String id;
    private final String name;
    private final String description;
    // 3+ -> Transport
    private final TransportDefinition transport;
    private final List<NodeDefinition> nodes;
    private final List<ConnectionDefinition> connections;

    public FlowDefinition(String id, String name, TransportDefinition transport, String description, List<NodeDefinition> nodes, List<ConnectionDefinition> connections) {
        if(id == null || id.isBlank()) {
            throw new FlowParserException("Flow ID 필수 입력");
        }

        if(nodes == null || nodes.isEmpty()) {
            throw new FlowParserException("Flow는 반드시 하나 이상의 노드를 포함");
        }

        this.id = id;
        this.name = name;
        this.description = description;
        this.transport = transport;
        this.nodes = List.copyOf(nodes);
        this.connections = connections == null ? List.of() : List.copyOf(connections);

        validateNode();
    }

    private void validateNode() {
        // 노드 ID 중복 확인
        Set<String> nodeIds = new HashSet<>();
        for (NodeDefinition node : nodes) {
            if (!nodeIds.add(node.getId())) {
                throw new FlowParserException("노드 ID 중복: " + node.getId());
            }
        }

        // 실행 가능한 그래프인지(연결) 미리 보장
        for (ConnectionDefinition conn : connections) {
            if (!nodeIds.contains(conn.getFromNode())) {
                throw new FlowParserException("Unknown source node: " + conn.getFromNode());
            }
            if (!nodeIds.contains(conn.getToNode())) {
                throw new FlowParserException("Unknown target node: " + conn.getToNode());
            }
        }
    }
}
