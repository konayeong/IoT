package com.fbp.engine.node;

import com.fbp.engine.core.conn.LocalConnection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.utils.LogNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogNodeTest {
    private LogNode logNode;
    private LocalConnection connection;

    @BeforeEach
    void setUp() {
        logNode = new LogNode("log");
        connection = new LocalConnection("conn");
        logNode.getOutputPort("out").connect(connection);
    }

    @Test
    @DisplayName("메시지 통과 전달")
    void message_origin_send() throws InterruptedException {
        Message msg = new Message(Map.of("tick", 5));
        logNode.process(msg);

        Message received = connection.poll();
        assertNotNull(received);
        assertEquals(5, received.getPayload().get("tick"));
    }

    @Test
    @DisplayName("중간 삽입 가능")
    void middle_insert() throws InterruptedException {
        LocalConnection connB = new LocalConnection("connB");

        logNode.getOutputPort("out").connect(connB);

        Message msg = new Message(Map.of("value", 42));
        logNode.process(msg); // LogNode가 중간 역할

        Message received = connB.poll();
        assertNotNull(received);
        assertEquals(42, received.getPayload().get("value"));
    }
}