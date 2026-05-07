package com.fbp.engine.node;

import com.fbp.engine.core.conn.LocalConnection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.utils.ThresholdFilterNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ThresholdFilterNodeTest {

    private ThresholdFilterNode node;
    private LocalConnection conn1;
    private LocalConnection conn2;

    @BeforeEach
    void setUp() {
        node = new ThresholdFilterNode("filter", "check", 30.0);
        conn1 = new LocalConnection("conn1");
        conn2 = new LocalConnection("conn2");

        node.getOutputPort("alert").connect(conn1);
        node.getOutputPort("normal").connect(conn2);
    }

    @Test
    @DisplayName("초과 -> alert 포트")
    void alert_port() {
        node.onProcess(new Message(Map.of("check", 35.0)));
        assertEquals(1, conn1.getBufferSize());
        assertEquals(0, conn2.getBufferSize());
    }

    @Test
    @DisplayName("이하 -> normal 포트")
    void normal_port() {
        node.onProcess(new Message(Map.of("check", 29.0)));
        assertEquals(0, conn1.getBufferSize());
        assertEquals(1, conn2.getBufferSize());
    }

    @Test
    @DisplayName("경계값(정확히 같은 값)")
    void normal_port_equals() {
        node.onProcess(new Message(Map.of("check", 30.0)));
        assertEquals(0, conn1.getBufferSize());
        assertEquals(1, conn2.getBufferSize());
    }

    @Test
    @DisplayName("키 없는 메시지")
    void not_exists_key() {
        assertDoesNotThrow(() -> node.onProcess(new Message(Map.of())));
    }

    @Test
    @DisplayName("양쪽 동시 검증")
    void two_verify() {
       // TODO ?
    }
}