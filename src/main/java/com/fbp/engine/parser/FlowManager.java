package com.fbp.engine.parser;

import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.core.Node;
import com.fbp.engine.registry.NodeRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FlowDefinition → Runtime Flow 생성
 * +
 * 배포/시작/중지/삭제 관리
 */
@Slf4j
@RequiredArgsConstructor
public class FlowManager {

    private final NodeRegistry nodeRegistry;
    private final FlowEngine flowEngine;
    private final Map<String, Flow> deployedFlows = new ConcurrentHashMap<>();

    /**
     * Flow 배포 및 실행
     */
    public void deploy(FlowDefinition definition) {

        if (definition == null) {
            throw new IllegalArgumentException("FlowDefinition은 null일 수 없습니다.");
        }

        String flowId = definition.id();

        // 중복 배포 방지
        if (deployedFlows.containsKey(flowId)) {
            throw new IllegalStateException("이미 존재하는 Flow ID: " + flowId);
        }

        log.info("Flow 배포 시작: {} ({})", definition.name(), flowId);

        // Runtime Flow 생성
        Flow flow = new Flow(definition.id());

        // Node 생성
        for (NodeDefinition nodeDef : definition.nodes()) {
            Node node;
            try {
                node = nodeRegistry.create(nodeDef.type(), nodeDef.id(), nodeDef.config());
            } catch (Exception e) {
                throw new IllegalStateException("노드 생성 실패: " + nodeDef.type(), e);
            }

            if (!(node instanceof AbstractNode absNode)) {
                throw new IllegalStateException("Node는 AbstractNode를 상속해야 함: " + nodeDef.type());
            }

            flow.addNode(absNode);
        }

        // Connection 연결
        for (ConnectionDefinition connDef : definition.connections()) {
            flow.connect(connDef.fromNode(), connDef.fromPort(), connDef.toNode(), connDef.toPort());
        }

        // Flow 검증
        if (!flow.validate().isEmpty()) {
            throw new IllegalStateException("Flow validation 실패: " + flow.validate());
        }

        // 엔진 등록
        flowEngine.register(flow);
        flowEngine.startFlow(flowId);
        deployedFlows.put(flowId, flow);

        log.info("Flow 배포 완료: {}", flowId);
    }

    /**
     * Flow 정지
     */
    public void stop(String flowId) {
        Flow flow = getRequiredFlow(flowId);

        if (flow.getFlowState() == Flow.FlowState.STOPPED) {
            return;
        }

        flowEngine.stopFlow(flowId);

        log.info("Flow 정지 완료: {}", flowId);
    }

    /**
     * Flow 재시작
     */
    public void restart(String flowId) {
        getRequiredFlow(flowId);
        flowEngine.startFlow(flowId);

        log.info("Flow 재시작 완료: {}", flowId);
    }

    /**
     * Flow 제거
     * 실행 중이면 자동 stop 후 제거
     */
    public void remove(String flowId) {
        Flow flow = getRequiredFlow(flowId);

        // 실행 중이면 자동 정지
        if (flow.getFlowState() == Flow.FlowState.RUNNING) {
            flowEngine.stopFlow(flowId);
        }

        // 저장소 제거
        deployedFlows.remove(flowId);

        log.info("Flow 제거 완료: {}", flowId);
    }

    public Flow.FlowState getStatus(String flowId) {
        return getRequiredFlow(flowId).getFlowState();
    }

    public Flow getFlow(String flowId) {
        return getRequiredFlow(flowId);
    }

    public Collection<Flow> list() {
        return Collections.unmodifiableCollection(deployedFlows.values());
    }

    private Flow getRequiredFlow(String flowId) {
        Flow flow = deployedFlows.get(flowId);

        if (flow == null) {
            throw new IllegalArgumentException("존재하지 않는 Flow ID: " + flowId);
        }

        return flow;
    }
}