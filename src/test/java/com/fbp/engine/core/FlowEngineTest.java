package com.fbp.engine.core;

import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.node.FilterNode;
import com.fbp.engine.node.PrintNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

class FlowEngineTest {

    private FlowEngine flowEngine;
    private Flow flow;

    @BeforeEach
    void setUp() {
        MetricsCollector metricsCollector = new MetricsCollector();
        flowEngine = new FlowEngine(metricsCollector);

        flow = new Flow("flow-test");
        flow.addNode(new FilterNode("filter", "tick", 3))
                .addNode(new PrintNode("print"))
                .connect("filter", "out", "print", "in");

        flowEngine.register(flow);
    }

    @Test
    @DisplayName("초기 상태")
    void state_initialized() {
        assertEquals(FlowEngine.State.INITIALIZED, flowEngine.getState());
    }

    @Test
    @DisplayName("플로우 등록")
    void register_flow() {
        assertEquals(flow, flowEngine.getFlows().get(flow.getId()));
    }

    @Test
    @DisplayName("startFlow 정상")
    void startFlow_success() {
        flowEngine.startFlow(flow.getId());
        assertEquals(FlowEngine.State.RUNNING, flowEngine.getState());
    }

    @Test
    @DisplayName("startFlow-없는 ID")
    void startFlow_notFoundId() {
        assertThrows(IllegalArgumentException.class, () -> flowEngine.startFlow("not"));
    }

    @Test
    @DisplayName("startFlow - 유효성 실패")
    void startFlow_validate_fail() {
        // validate 에러가 있는 Flow
        Flow flow2 = new Flow("flow2");
        flowEngine.register(flow2);
        assertThrows(IllegalStateException.class, () -> {
            flowEngine.startFlow("flow2");
        });
    }

    @Test
    @DisplayName("stopFlow 정상")
    void stopFlow_success() {
        flowEngine.stopFlow(flow.getId());
        assertEquals(Flow.FlowState.STOPPED, flow.getFlowState());
    }

    @Test
    @DisplayName("shutdown 전체")
    void shutdown_all() {
        flowEngine.shutdown();
        assertEquals(FlowEngine.State.STOPPED, flowEngine.getState());
    }

    @Test
    @DisplayName("다중 플로우 독립 동작")
    void multi_flow() {
        Flow flow2 = new Flow("flow2");
        flow2.addNode(new FilterNode("filter2", "tick", 3))
                .addNode(new PrintNode("print2"))
                .connect("filter2", "out", "print2", "in");

        flowEngine.register(flow2);

        flowEngine.startFlow(flow.getId());
        flowEngine.startFlow(flow2.getId());

        flowEngine.stopFlow(flow.getId());

        assertEquals(Flow.FlowState.STOPPED, flow.getFlowState());
        assertEquals(Flow.FlowState.RUNNING, flow2.getFlowState());

    }

    @Test
    @DisplayName("listFlows 출력")
    void list() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        flowEngine.listFlows();

        String output = out.toString();

        assertTrue(output.contains("flow-test"));
        assertTrue(output.contains("STOPPED"));
    }

}