package com.fbp.engine.core;

import com.fbp.engine.core.conn.LocalConnection;
import com.fbp.engine.core.port.InputPort;
import com.fbp.engine.core.port.OutputPort;
import com.fbp.engine.node.AbstractNode;
import lombok.Getter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 노드와 연결을 하나의 단위로 묶어 관리
@Getter
public class Flow {
    // 노드 방문 상태 (순환 판단)
    private enum State {
        UNVISITED,
        VISITING,
        VISITED
    }

    public enum FlowState {
        RUNNING,
        STOPPED
    }

    private final String id;
    private String name;
    private String description;

    private final Map<String, AbstractNode> nodes = new HashMap<>(); // 등록된 노드
    private final List<LocalConnection> connections = new ArrayList<>(); // 생성된 연결
    private FlowState flowState;

    public Flow(String id) {
        this.id = id;
        this.flowState = FlowState.STOPPED;
    }
    public Flow(String id, String name, String description) {
        this(id);
        this.name = name;
        this.description = description;
    }

    public Flow addNode(AbstractNode node) {
        nodes.put(node.getId(), node);
        return this; // 메서드 체이닝 지원
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
        // connect 생성
        String connId = String.format("%s:%s->%s:%s", sourceNodeId, sourcePort, targetNodeId, targetPort);

        // 3+ (BridgeConnectionFactory)

        LocalConnection connection = new LocalConnection(connId);
        connection.setTarget(targetNodePort);
        sourceNodePort.connect(connection);

        connections.add(connection);

        return this;
    }

    public void initialize() {
        for(AbstractNode node : nodes.values()) {
            node.initialize();
        }
        this.flowState = FlowState.RUNNING;
    }

    public void shutdown() {
        for(AbstractNode node : nodes.values()) {
            node.shutdown();
        }
        this.flowState = FlowState.STOPPED;
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if(nodes.isEmpty()) {
            errors.add("등록된 노드가 없습니다.");
        }

        // connect
        for(LocalConnection conn : connections) {
            String id = conn.getId();

            String[] parts = id.split("->");
            String sourceNodeId = parts[0].split(":")[0];
            String targetNodeId = parts[1].split(":")[0];

            if (!nodes.containsKey(sourceNodeId)) {
                errors.add("존재하지 않는 source 노드: " + sourceNodeId);
            }

            if (!nodes.containsKey(targetNodeId)) {
                errors.add("존재하지 않는 target 노드: " + targetNodeId);
            }
        }

        // 순환 참조 탐지
        if(hasCycle()) {
            errors.add("순환 참조 발생");
        }
        return errors;
    }

    private boolean hasCycle() {
        Map<String, List<String>> graph = new HashMap<>();

        for (String id : nodes.keySet()) {
            graph.put(id, new ArrayList<>());
        }

        for (LocalConnection conn : connections) {
            String[] parts = conn.getId().split("->");
            String source = parts[0].split(":")[0];
            String target = parts[1].split(":")[0];
            graph.get(source).add(target);
        }

        // 각 노드의 상태
        Map<String, State> state = new HashMap<>();
        for (String id : nodes.keySet()) {
            state.put(id, State.UNVISITED);
        }

        // DFS 실행
        for (String id : nodes.keySet()) {
            if (state.get(id) == State.UNVISITED) {
                if (dfs(id, graph, state)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean dfs(String node,
                        Map<String, List<String>> graph,
                        Map<String, State> state) {

        state.put(node, State.VISITING);

        for (String next : graph.get(node)) {
            // 아직 안 간 노드 → 계속 탐색
            if (state.get(next) == State.UNVISITED) {
                if (dfs(next, graph, state)) return true;
            }
            // 지금 탐색 중인 노드 다시 만나면 → 사이클
            else if (state.get(next) == State.VISITING) {
                return true;
            }
        }

        state.put(node, State.VISITED);
        return false;
    }
}