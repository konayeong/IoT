package com.fbp.engine.node.utils;

import com.fbp.engine.core.conn.LocalConnection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CounterNodeTest {

    private CounterNode counterNode;
    private LocalConnection connection;

    @BeforeEach
    void setUp() {
        counterNode = new CounterNode("counter");
        connection = new LocalConnection("conn");

        counterNode.getOutputPort("out").connect(connection);
    }

    @Test
    @DisplayName("count 키 추가")
    void add_count() {
        Message message = new Message(Map.of("temperature", 20));
        counterNode.onProcess(message);
        assertEquals(1, connection.poll().getPayload().get("count"));
    }

    @Test
    @DisplayName("count 누적")
    void counts() {
        counterNode.onProcess(new Message(Map.of("msg1", "value1")));
        counterNode.onProcess(new Message(Map.of("msg2", "value2")));
        counterNode.onProcess(new Message(Map.of("msg3", "value3")));

        connection.poll();
        connection.poll();
        assertEquals(3, connection.poll().getPayload().get("count"));
    }

    @Test
    @DisplayName("원본 키 유지")
    void origin_key() {
        Message message = new Message(Map.of("temperature", 20));
        counterNode.onProcess(message);

        assertEquals(20, connection.poll().getPayload().get("temperature"));
    }

}