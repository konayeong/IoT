package com.fbp.engine.engine;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.core.conn.Connection;
import com.fbp.engine.node.AbstractNode;
import com.fbp.engine.parser.ConnectionDefinition;
import com.fbp.engine.parser.FlowDefinition;
import com.fbp.engine.parser.NodeDefinition;
import com.fbp.engine.parser.TransportDefinition;
import com.fbp.engine.registry.NodeRegistry;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 동적 플로우 관리
 * 플로우 라이프사이클 (deploy / start / stop / restart / remove)
 * 런타임 변경 (노드 추가/제거, 연결 추가/제거, 설정 변경)
 * BridgeConnectionFactory -> Local / MqttBridge Connection
 */
public class FlowManager {

    private final NodeRegistry nodeRegistry;
    private final FlowEngine flowEngine;

    public FlowManager(NodeRegistry nodeRegistry, FlowEngine flowEngine) {
        this.nodeRegistry = nodeRegistry;
        this.flowEngine = flowEngine;
    }

    // FlowEngine에 등록 및 실행
    public void deploy(FlowDefinition definition) {
        String flowId = definition.getId();

        // Flow 생성
        Flow flow = new Flow(flowId);

        if (flowEngine.getFlows().containsKey(flowId)) {
            throw new IllegalStateException("이미 존재하는 Flow: " + flowId);
        }

        // Node 생성 및 등록
        for(NodeDefinition nodeDefinition : definition.getNodes()) {
            AbstractNode node = (AbstractNode) nodeRegistry.create(
                    nodeDefinition.getType(),
                    nodeDefinition.getConfig()
            );
            flow.addNode(node);
        }

        // Connection 연결 -> TODO (+3) BridgeConnectionFactory로 Connection 생성

        for(ConnectionDefinition conn : definition.getConnections()) {
            flow.connect(
                conn.getFromNode(),
                conn.getFromPort(),
                conn.getToNode(),
                conn.getToPort()
            );
        }

        // Flow 검증
        List<String> errors = flow.validate();
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Flow validation 실패: " + errors);
        }

        // 등록 + 실행
        flowEngine.register(flow);
        flowEngine.startFlow(flowId);
    }

    public void stop(String flowId) {
        flowEngine.stopFlow(flowId);
    }

    public void restart(String flowId) {
        flowEngine.stopFlow(flowId);
        flowEngine.startFlow(flowId);
    }

    public void remove(String flowId) {
        flowEngine.stopFlow(flowId);
        flowEngine.getFlows().remove(flowId);
    }

    public Flow.FlowState getStatus(String flowId) {
        return getFlow(flowId).getFlowState();
    }

    public Set<String> list() {
        return Collections.unmodifiableSet(flowEngine.getFlows().keySet());
    }

    private Flow getFlow(String flowId) {
        Flow flow = flowEngine.getFlows().get(flowId);
        if (flow == null) {
            throw new IllegalArgumentException("Flow가 존재하지 않음: " + flowId);
        }
        return flow;
    }

    public int flowSize() {
        return flowEngine.getFlows().size();
    }

}
