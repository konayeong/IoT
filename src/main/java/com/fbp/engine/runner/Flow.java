package com.fbp.engine.runner;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.InputPort;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.node.AbstractNode;
import lombok.Getter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Flow {
    private String id;
    @Getter
    private Map<String, AbstractNode> nodes;
    @Getter
    private List<Connection> connections;

    public Flow(String id) {
        this.id = id;
        nodes = new HashMap<>();
        connections = new ArrayList<>();
    }

    // 노드 등록
    public Flow addNode(AbstractNode node) {
        nodes.put(node.getId(), node);
        return this;
    }

    // 연결 생성 - "소스노드ID:포트이름" -> "대상노드ID:포트이름"
    public Flow connect(String sourceNodeId, String sourcePort, String targetNodeId, String targetPort) {
        // 노드 존재 확인
        AbstractNode sourceNode = nodes.get(sourceNodeId);
        AbstractNode targetNode = nodes.get(targetNodeId);

        if(sourceNode == null || targetNode == null) {
            throw new IllegalArgumentException("노드가 존재하지 않습니다.");
        }

        // 포트 존재 확인
        OutputPort sourceNodePort = sourceNode.getOutputPort(sourcePort);
        InputPort targetNodePort = targetNode.getInputPort(targetPort);

        if(sourceNodePort == null || targetNodePort == null) {
            throw new IllegalArgumentException("포트가 존재하지 않습니다.");
        }
        // connect 생성 ?!
        String connId = String.format("%s:%s->%s:%s", sourceNodeId, sourcePort, targetNodeId, targetPort);
        Connection connection = new Connection(connId);
        connection.setTarget(targetNodePort);
        sourceNodePort.connect(connection);

        connections.add(connection);

        return this;
    }

    public void initialize() {
        for(AbstractNode node : nodes.values()) {
            node.initialize();
        }
    }

    public void shutdown() {
        for(AbstractNode node : nodes.values()) {
            node.shutdown();
        }
    }
}
