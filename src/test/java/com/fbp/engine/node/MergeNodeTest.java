package com.fbp.engine.node;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.utils.MergeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MergeNodeTest {

    private MergeNode node;
    private Connection conn;

    @BeforeEach
    void setUp() {
        node = new MergeNode("merge");
        conn = new Connection("conn");
        node.getOutputPort("out").connect(conn);
    }

    @Test
    @DisplayName("양쪽 입력 수신")
    void receive_both_inputs() {
        Message m1 = new Message(Map.of("inputPort", "in-1", "A", "a"));
        Message m2 = new Message(Map.of("inputPort", "in-2", "One", 1));

        node.onProcess(m1);
        node.onProcess(m2);

        Message result = conn.poll();

        assertNotNull(result);
    }

    @Test
    @DisplayName("합쳐진 메시지 출력")
    void merge_messages() {
        Message m1 = new Message(Map.of("inputPort", "in-1", "A", "a"));
        Message m2 = new Message(Map.of("inputPort", "in-2", "One", 1));

        node.onProcess(m1);
        node.onProcess(m2);

        Message result = conn.poll();

        assertEquals("a", result.getPayload().get("A"));
        assertEquals(1, result.getPayload().get("One"));
    }

    @Test
    @DisplayName("한쪽만 도착 시 대기")
    void wait_if_one_side_missing() {
        Message m1 = new Message(Map.of("inputPort", "in-1", "A", "a"));

        node.onProcess(m1);

        assertTrue(conn.getBufferSize() == 0);
    }

    @Test
    @DisplayName("포트 구성 확인")
    void ports_exist() {
        assertNotNull(node.getInputPort("in-1"));
        assertNotNull(node.getInputPort("in-2"));
        assertNotNull(node.getOutputPort("out"));
    }
}