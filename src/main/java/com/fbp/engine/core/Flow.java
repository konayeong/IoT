package com.fbp.engine.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
public class Flow {
    // 노드 방문 상태
    private enum State {
        UNVISITED,
        VISITING,
        VISITED
    }

    // Flow 개별 상태 관리
    public enum FlowState {
        RUNNING,
        STOPPED
    }

    private final String id;
    private String name;
    private String description;

    private final Map<String, AbstractNode> nodes = new HashMap<>();
    private final List<FlowConnection> connections = new ArrayList<>();
    @Setter
    private FlowState flowState =  FlowState.STOPPED;

    public Flow(String id) {
        this.id = id;
    }

    public Flow(String id, String name, String description) {
        // step8
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Flow addNode(AbstractNode node) {
        nodes.put(node.getId(), node);
        return this; // 메서드 체이닝 지원
    }

    public Flow connect(String sourceNodeId, String sourcePort, String targetNodeId, String targetPort) {
        log.info("connect {} → {}", sourceNodeId, targetNodeId);
        AbstractNode sourceNode = nodes.get(sourceNodeId);
        AbstractNode targetNode = nodes.get(targetNodeId);

        if (sourceNode == null) {
            throw new IllegalArgumentException("Source node not found: " + sourceNodeId);
        }
        if (targetNode == null) {
            throw new IllegalArgumentException("Target node not found: " + targetNodeId);
        }

        OutputPort out = sourceNode.getOutputPort(sourcePort);
        InputPort in = targetNode.getInputPort(targetPort);

        if (out == null) {
            throw new IllegalArgumentException("Source port not found: " + sourcePort);
        }
        if (in == null) {
            throw new IllegalArgumentException("Target port not found: " + targetPort);
        }

        String connId = sourceNodeId + ":" + sourcePort + "->" + targetNodeId + ":" + targetPort;
        Connection connection = new LocalConnection(connId);
        connection.setTarget(in);

        out.connect(connection);

        connections.add(new FlowConnection(sourceNodeId, sourcePort, targetNodeId, targetPort, connection));

        return this;
    }

    public void addConnection(String sourceNodeId, String sourcePort, String targetNodeId, String targetPort, Connection connection) {
        AbstractNode sourceNode = nodes.get(sourceNodeId);
        AbstractNode targetNode = nodes.get(targetNodeId);

        OutputPort out = sourceNode.getOutputPort(sourcePort);
        InputPort in = targetNode.getInputPort(targetPort);

        connection.setTarget(in);

        out.connect(connection);

        connections.add(new FlowConnection(sourceNodeId, sourcePort, targetNodeId, targetPort, connection));
    }

    public void initialize() {
        for(AbstractNode node : nodes.values()) {
            node.initialize();
        }
        this.flowState = FlowState.RUNNING;
    }

    public void shutdown() {

        for (FlowConnection fc : connections) {
            fc.getConnection().close();
        }

        for (AbstractNode node : nodes.values()) {
            node.shutdown();
        }

        this.flowState = FlowState.STOPPED;
    }

    public void removeNode(String nodeId) {

        AbstractNode node = nodes.remove(nodeId);

        if (node == null) {
            return;
        }

        // 연결 제거
        List<String> removeIds = new ArrayList<>();

        for (FlowConnection fc : connections) {

            if (fc.getSourceNodeId().equals(nodeId)
                    || fc.getTargetNodeId().equals(nodeId)) {

                removeIds.add(fc.getId());
            }
        }

        for (String connId : removeIds) {
            removeConnection(connId);
        }

        node.shutdown();
    }

    public void removeConnection(String connectionId) {

        FlowConnection target = null;

        for (FlowConnection fc : connections) {

            if (fc.getId().equals(connectionId)) {
                target = fc;
                break;
            }
        }

        if (target == null) {
            return;
        }

        AbstractNode sourceNode = nodes.get(target.getSourceNodeId());

        if (sourceNode != null) {

            OutputPort out = sourceNode.getOutputPort(target.getSourcePort());

            if (out != null) {
                out.disconnect(target.getConnection());
            }
        }

        target.getConnection().close();

        connections.remove(target);
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if(nodes.isEmpty()) { // 노드가 0개면 에러
            errors.add("등록된 노드가 없습니다.");
            return errors;
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

        for (FlowConnection fc : connections) {
            Connection conn = fc.getConnection();
            // MQTT bridge 같은 외부 connection은 제외
            if (!conn.getId().contains("->")) {
                continue;
            }

            String[] parts = conn.getId().split("->");

            String source = parts[0].split(":")[0];
            String target = parts[1].split(":")[0];

            if (!graph.containsKey(source) || !graph.containsKey(target)) {
                continue;
            }
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

    private boolean dfs(String node, Map<String, List<String>> graph, Map<String, State> state) {

        state.put(node, State.VISITING);

        for (String next : graph.getOrDefault(node, List.of())) {
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
