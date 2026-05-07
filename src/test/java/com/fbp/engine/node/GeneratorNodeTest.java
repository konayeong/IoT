package com.fbp.engine.node;

import com.fbp.engine.core.conn.LocalConnection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.out.GeneratorNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorNodeTest {
    private GeneratorNode node;
    private LocalConnection conn;

    @BeforeEach
    void setUp() {
        node = new GeneratorNode("generator");
        conn = new LocalConnection("conn");
        node.getOutputPort("out").connect(conn);
    }

    @Test
    @DisplayName("generate 메시지 생성")
    void create_message() {
        node.generate("key", "value");

        assertNotNull(conn.poll());
    }

    @Test
    @DisplayName("메시지 내용 확인")
    void message_check() {
        node.generate("key", "value");
        Message msg = conn.poll();
        assertTrue(msg.getPayload().containsKey("key"));
        assertTrue(msg.getPayload().containsValue("value"));
    }

    @Test
    @DisplayName("OutputPort 조회")
    void outputPort() {
        assertNotNull(node.getOutputPort("out"));
    }

    @Test
    @DisplayName("다수 generate 호출")
    void multi_generate() {
        node.generate("A", "a");
        node.generate("B", "b");
        node.generate("C", "c");

        assertEquals("a", conn.poll().getPayload().get("A"));
        assertEquals("b", conn.poll().getPayload().get("B"));
        assertEquals("c", conn.poll().getPayload().get("C"));
    }
}