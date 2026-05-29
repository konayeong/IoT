package com.fbp.engine.node;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.LocalConnection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DelayNodeTest {

    private final long delayMs = 1000;
    private DelayNode delayNode;
    private Connection connection;

    @BeforeEach
    void setUp() {
        delayNode = new DelayNode("delay", delayMs);
        connection = new LocalConnection("conn");
        delayNode.getOutputPort("out").connect(connection);
    }

    @Test
    @DisplayName("지연 후 전달")
    void delay_send() {
        long startTime = System.currentTimeMillis();
        delayNode.onProcess(new Message(Map.of()));
        long endTime = System.currentTimeMillis();

        assertTrue(delayMs < endTime-startTime);
    }

    @Test
    @DisplayName("메시지 내용 보존")
    void message_remain() throws InterruptedException {
        Message message = new Message(Map.of("key", "value"));
        delayNode.onProcess(message);
        assertEquals(message, connection.poll());
    }

}