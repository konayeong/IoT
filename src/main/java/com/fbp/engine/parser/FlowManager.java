package com.fbp.engine.parser;

import com.fbp.engine.api.FlowNotFoundException;
import com.fbp.engine.core.*;
import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.registry.NodeRegistry;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private final MetricsCollector metricsCollector;
    private final ConnectionFactory connectionFactory;

    /**
     * Flow 배포 및 실행
     */
    public Flow deploy(FlowDefinition definition) {

        if (definition == null) {
            throw new IllegalArgumentException("FlowDefinition은 null일 수 없습니다.");
        }

        String flowId = definition.id();

        // 중복 배포 방지
        if (deployedFlows.containsKey(flowId)) {
            throw new DuplicateRequestException("이미 존재하는 Flow ID: " + flowId);
        }

        // Runtime Flow 생성
        Flow flow = new Flow(definition.id(), definition.name(), definition.description());

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

        Set<String> nodeIds = definition.nodes()
                .stream()
                .map(NodeDefinition::id)
                .collect(Collectors.toSet());

        metricsCollector.registerFlow(flowId, nodeIds);

        // Connection 연결
        for (ConnectionDefinition connDef : definition.connections()) {

            Connection connection = connectionFactory.create(definition, connDef);

            flow.addConnection(
                    connDef.fromNode(),
                    connDef.fromPort(),
                    connDef.toNode(),
                    connDef.toPort(),
                    connection
            );
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

        return flow;
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
    public boolean remove(String flowId) {
        Flow flow = deployedFlows.get(flowId);
        if(flow == null) {
            throw new IllegalArgumentException();
        }

        // 실행 중이면 자동 정지
        if (flow.getFlowState() == Flow.FlowState.RUNNING) {
            flowEngine.stopFlow(flowId);
        }

        // 저장소 제거
        deployedFlows.remove(flowId);
        flow.shutdown();
        log.info("Flow 제거 완료: {}", flowId);
        return true;
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

    // == Step8 ==
    // 실행 중인 플로우 목록
    public List<Flow> getRunningFlows() {
        return deployedFlows.values().stream()
                .filter(f -> f.getFlowState() == Flow.FlowState.RUNNING).toList();
    }

    // Engine Status
    public String getEngineStatus() {
        return flowEngine.getState().name();
    }

    // Engine startTime
    public long getEngineStart() {
        return flowEngine.getStart();
    }

    // 실행 중인 flow 개수
    public int flowSize() {
        return deployedFlows.values().stream()
                .filter(f -> f.getFlowState() == Flow.FlowState.RUNNING).toList().size();
    }

    private Flow getRequiredFlow(String flowId) {
        Flow flow = deployedFlows.get(flowId);

        if (flow == null) {
            throw new FlowNotFoundException(flowId);
        }

        return flow;
    }
}