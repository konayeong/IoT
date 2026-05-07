package com.fbp.engine.node;

import com.fbp.engine.core.conn.LocalConnection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.out.TimerNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimerNodeTest {

    private TimerNode timerNode;
    private LocalConnection connection;

    @BeforeEach
    void setUp() {
        timerNode = new TimerNode("timer", 500);
        connection = new LocalConnection("conn");
        timerNode.getOutputPort("out").connect(connection);
    }

    @AfterEach
    void tearDown() {
        timerNode.shutdown();
    }

    @Test
    @DisplayName("initialize 후 메시지 생성")
    void initialize() throws InterruptedException {
        timerNode.initialize();
        Thread.sleep(1000);
        assertNotNull(connection.poll());
    }

    @Test
    @DisplayName("tick 증가")
    void tick() throws InterruptedException {
        timerNode.initialize();
        Thread.sleep(2100);
        int expectedTick = 0;
        while (connection.getBufferSize() > 0) {
            Message msg = connection.poll();
            assertEquals(expectedTick, msg.getPayload().get("tick"));
            expectedTick++;
        }
    }

    @Test
    @DisplayName("shotdown 후 정지")
    void shutdown() throws InterruptedException {
        timerNode.initialize();
        Thread.sleep(1100);
        timerNode.shutdown();
        int before = connection.getBufferSize();
        Thread.sleep(600);
        int after = connection.getBufferSize();
        assertEquals(before, after);
    }

    @Test
    @DisplayName("주기 확인")
    void message_period() throws InterruptedException {
        timerNode.initialize();
        Thread.sleep(2100);
        int count = connection.getBufferSize();
        assertTrue(count >= 4 && count <= 5);
    }
}