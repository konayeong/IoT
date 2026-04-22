package com.fbp.engine.node;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.out.GeneratorNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CollectorNodeTest {

    private CollectorNode node;

    @BeforeEach
    void setUp() {
        node = new CollectorNode("collector");
    }

    @Test
    @DisplayName("메시지 수집")
    void message_collector() {
        node.onProcess(new Message(Map.of("key", "value")));
        assertEquals(1, node.getCollected().size());
        assertEquals("value", node.getCollected().get(0).getPayload().get("key"));
    }

    @Test
    @DisplayName("수집 순서 보존")
    void collector_sort() {
        node.onProcess(new Message(Map.of("One", 1)));
        node.onProcess(new Message(Map.of("Two", 2)));
        node.onProcess(new Message(Map.of("Three", 3)));

        List<Message> collected = node.getCollected();
        assertAll(
                () -> assertEquals(1, (Integer) collected.get(0).get("One")),
                () -> assertEquals(2, (Integer) collected.get(1).get("Two")),
                () -> assertEquals(3, (Integer) collected.get(2).get("Three"))
        );
    }

    @Test
    @DisplayName("초기 상태 빈 리스트")
    void init() {
        assertTrue(node.getCollected().isEmpty());
    }

    @Test
    @DisplayName("InputPort 존재")
    void exists_inputPort() {
        assertNotNull(node.getInputPort("in"));
    }

    @Test
    @DisplayName("파이프라인 연결 검증")
    void pipeline() throws InterruptedException {
        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("pipeline-test");

        GeneratorNode generator = new GeneratorNode("gen");
        CollectorNode collector = new CollectorNode("collector");

        flow.addNode(generator)
            .addNode(collector)
            .connect("gen", "out", "collector", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        generator.generate("key1", "value1");
        generator.generate("key2", "value2");
        generator.generate("key3", "value3");

        Thread.sleep(5000);

        engine.shutdown();

        assertEquals(3, collector.getCollected().size());
    }
}