package com.fbp.engine.engine;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.core.conn.Connection;
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
    private final ConnectionFactory connectionFactory;

    // FlowEngine에 등록 및 실행
    public Flow deploy(FlowDefinition definition) {
        String flowId = definition.getId();

        if (flowEngine.getFlows().containsKey(flowId)) {
            throw new IllegalStateException("이미 존재하는 Flow: " + flowId);
        }

        // Flow 생성
        Flow flow = new Flow(flowId, definition.getName(), definition.getDescription());

        // Node 생성
        for(NodeDefinition nodeDefinition : definition.getNodes()) {
            AbstractNode node = (AbstractNode) nodeRegistry.create(
                    nodeDefinition.type(),
                    nodeDefinition.config()
            );
            node.setMetricsCollector(metricsCollector);
            flow.addNode(node);
        }

        // Connection 연결 -> BridgeConnectionFactory를 통해 Local/MQTT 동적 분기 생성
        for(ConnectionDefinition conn : definition.getConnections()) {
            Connection connection = connectionFactory.create(definition, conn);
            flow.connect(
                    conn.fromNode(),
                    conn.fromPort(),
                    conn.toNode(),
                    conn.toPort(),
                    connection
            );
        }

        // Flow 검증
        List<String> errors = flow.validate();
        if (!errors.isEmpty()) {
            // 실패 시 생성된 커넥션 정리 자원 해제 루프 필요 (선택)
            throw new IllegalStateException("Flow validation 실패: " + errors);
        }

        // 등록 + 실행
        flowEngine.register(flow);
        flowEngine.startFlow(flowId);

        return flow;
    }

    public void stop(String flowId) {
        flowEngine.stopFlow(flowId);
        releaseFlowConnections(flowId);
    }

    public void restart(String flowId) {
        stop(flowId);
        flowEngine.startFlow(flowId);
    }

    // MetricsHandler 및 완전 삭제
    public boolean remove(String flowId) {
        if (!flowEngine.getFlows().containsKey(flowId)) {
            return false;
        }

        stop(flowId);
        flowEngine.getFlows().remove(flowId);

        return true;
    }

    //플로우 내부에 주입된 모든 커넥션을 찾아 close()를 호출해 주는 헬퍼 메서드
    private void releaseFlowConnections(String flowId) {
        Flow flow = flowEngine.getFlows().get(flowId);
        if (flow != null && flow.getConnections() != null) {
            for (Connection conn : flow.getConnections()) {
                try {
                    conn.close();
                } catch (Exception e) {
                    System.err.println("커넥션 자원 해제 중 예외 발생: " + e.getMessage());
                }
            }
        }
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