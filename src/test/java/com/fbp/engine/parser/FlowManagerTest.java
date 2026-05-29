package com.fbp.engine.parser;

import com.fbp.engine.api.FlowNotFoundException;
import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.core.BridgeConnectionFactory;
import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.registry.NodeRegistry;
import com.sun.jdi.request.DuplicateRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class FlowManagerTest {

    private NodeRegistry nodeRegistry;
    private FlowEngine flowEngine;
    private FlowManager flowManager;
    private MetricsCollector metricsCollector;

    // 테스트용 노드
    static class TestNode extends AbstractNode {

        protected TestNode(String id, Map<String, Object> config) {
            super(id);
        }

        @Override
        protected void onProcess(Message message) {
        }
    }

    @BeforeEach
    void setUp() {
        nodeRegistry = new NodeRegistry();
        metricsCollector = new MetricsCollector();

        nodeRegistry.register(
                "test",
                TestNode::new
        );

        flowEngine = new FlowEngine(metricsCollector);

        flowManager = new FlowManager(
                nodeRegistry,
                flowEngine,
                metricsCollector,
                new BridgeConnectionFactory()
        );
    }

    @Test
    @DisplayName("deploy")
    void deploy_flow() {

        FlowDefinition definition = createFlowDefinition("flow1");

        flowManager.deploy(definition);

        assertEquals(
                Flow.FlowState.RUNNING,
                flowManager.getStatus("flow1")
        );
    }

    @Test
    @DisplayName("list")
    void list_flows() {

        flowManager.deploy(createFlowDefinition("flow1"));
        flowManager.deploy(createFlowDefinition("flow2"));

        assertEquals(
                2,
                flowManager.list().size()
        );
    }

    @Test
    @DisplayName("getStatus")
    void get_status() {

        flowManager.deploy(createFlowDefinition("flow1"));

        assertEquals(
                Flow.FlowState.RUNNING,
                flowManager.getStatus("flow1")
        );
    }

    @Test
    @DisplayName("stop")
    void stop_flow() {

        flowManager.deploy(createFlowDefinition("flow1"));

        flowManager.stop("flow1");

        assertEquals(
                Flow.FlowState.STOPPED,
                flowManager.getStatus("flow1")
        );
    }

    @Test
    @DisplayName("restart")
    void restart_flow() {

        flowManager.deploy(createFlowDefinition("flow1"));

        flowManager.stop("flow1");

        flowManager.restart("flow1");

        assertEquals(
                Flow.FlowState.RUNNING,
                flowManager.getStatus("flow1")
        );
    }

    @Test
    @DisplayName("remove")
    void remove_flow() {

        flowManager.deploy(createFlowDefinition("flow1"));

        flowManager.remove("flow1");

        assertTrue(
                flowManager.list().isEmpty()
        );
    }

    @Test
    @DisplayName("실행 중 삭제")
    void remove_running_flow() {

        flowManager.deploy(createFlowDefinition("flow1"));

        assertEquals(
                Flow.FlowState.RUNNING,
                flowManager.getStatus("flow1")
        );

        flowManager.remove("flow1");

        assertTrue(
                flowManager.list().isEmpty()
        );
    }

    @Test
    @DisplayName("존재하지 않는 ID 조작")
    void manipulate_unknown_flow() {
        assertThrows(FlowNotFoundException.class, () -> flowManager.stop("unknown"));

        assertThrows(FlowNotFoundException.class, () -> flowManager.restart("unknown"));

        assertThrows(IllegalArgumentException.class, () -> flowManager.remove("unknown"));
    }

    @Test
    @DisplayName("중복 ID 배포")
    void duplicate_flow_id() {

        FlowDefinition definition = createFlowDefinition("flow1");

        flowManager.deploy(definition);

        assertThrows(DuplicateRequestException.class, () -> flowManager.deploy(definition));
    }

    @Test
    @DisplayName("미등록 노드 타입")
    void unregistered_node_type() {

        FlowDefinition definition = new FlowDefinition(
                "flow1",
                "test-flow",
                null,
                null,
                List.of(
                        new NodeDefinition(
                                "node1",
                                "unknown-type",
                                Map.of()
                        )
                ),
                List.of()
        );

        assertThrows(
                IllegalStateException.class,
                () -> flowManager.deploy(definition)
        );
    }

    private FlowDefinition createFlowDefinition(String flowId) {

        return new FlowDefinition(
                flowId,
                "test-flow",
                null,
                null,
                List.of(
                        new NodeDefinition(
                                "node1",
                                "test",
                                Map.of()
                        )
                ),
                List.of()
        );
    }
}