package com.fbp.engine.engine;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.node.AbstractNode;
import com.fbp.engine.parser.definition.ConnectionDefinition;
import com.fbp.engine.parser.definition.FlowDefinition;
import com.fbp.engine.parser.definition.NodeDefinition;
import com.fbp.engine.registry.NodeRegistry;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 동적 플로우 관리
 * 플로우 라이프사이클 (deploy / start / stop / restart / remove)
 * 런타임 변경 (노드 추가/제거, 연결 추가/제거, 설정 변경)
 * BridgeConnectionFactory -> Local / MqttBridge Connection
 */
@RequiredArgsConstructor
public class FlowManager {

    private final NodeRegistry nodeRegistry;
    private final FlowEngine flowEngine;
    private final MetricsCollector metricsCollector;

    // FlowEngine에 등록 및 실행
    public Flow deploy(FlowDefinition definition) {
        String flowId = definition.getId();

        // Flow 생성
        Flow flow = new Flow(flowId, definition.getName(), definition.getDescription());

        if (flowEngine.getFlows().containsKey(flowId)) {
            throw new IllegalStateException("이미 존재하는 Flow: " + flowId);
        }

        // Node 생성 및 등록
        for(NodeDefinition nodeDefinition : definition.getNodes()) {
            AbstractNode node = (AbstractNode) nodeRegistry.create(
                    nodeDefinition.getType(),
                    nodeDefinition.getConfig()
            );
            node.setMetricsCollector(metricsCollector);
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

        return flow;
    }

    public void stop(String flowId) {
        flowEngine.stopFlow(flowId);
    }

    public void restart(String flowId) {
        flowEngine.stopFlow(flowId);
        flowEngine.startFlow(flowId);
    }

    private Flow getFlow(String flowId) {
        Flow flow = flowEngine.getFlows().get(flowId);
        if (flow == null) {
            throw new IllegalArgumentException("Flow가 존재하지 않음: " + flowId);
        }
        return flow;
    }

    // FlowHandler
    public Map<String, Flow> getRunningFlows() {
        Map<String, Flow> runningFlow = new HashMap<>();
        for(Flow flow : flowEngine.getFlows().values()) {
            if(flow.getFlowState().equals(Flow.FlowState.RUNNING)) {
                runningFlow.put(flow.getId(), flow);
            }
        }
        return runningFlow;
    }

    // MetricsHandler
    public boolean remove(String flowId) {
        Flow removed = flowEngine.getFlows().remove(flowId);

        if(removed == null) {
            return false;
        }

        flowEngine.stopFlow(flowId);

        return true;
    }

    // HealthHandler
    public String getEngineStatus() {
        return flowEngine.getState().name();
    }

    public int flowSize() {
        return flowEngine.getFlows().size();
    }

    public long getEngineStart() {
        return flowEngine.getStartedAt();
    }

}
