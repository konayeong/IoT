package com.fbp.engine.node;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.LocalConnection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class SplitNodeTest {

    private SplitNode splitNode;
    private Connection connection1;
    private Connection connection2;

    @BeforeEach
    void setUp() {
        splitNode = new SplitNode("split", "tick", 3);

        connection1 = new LocalConnection("match");
        connection2 = new LocalConnection("mismatch");
        splitNode.getOutputPort("match").connect(connection1);
        splitNode.getOutputPort("mismatch").connect(connection2);
    }

    @Test
    @DisplayName("조건 만족 -> match 포트")
    void success_match() throws InterruptedException {
        Message send = new Message(Map.of("tick", 5));
        splitNode.onProcess(send);
        assertEquals(send, connection1.poll());
        assertEquals(0, connection2.getBufferSize());
    }

    @Test
    @DisplayName("조건 미달 -> mismatch 포트")
    void fail_mismatch() throws InterruptedException {
        Message send = new Message(Map.of("tick", 0));
        splitNode.onProcess(send);
        assertEquals(send, connection2.poll());
        assertEquals(0, connection1.getBufferSize());
    }

    @Test
    @DisplayName("양쪽 동시 확인")
    void match_mismatch() throws InterruptedException {
        Message send = new Message(Map.of("tick", 5));
        Message warnMsg = new Message(Map.of("tick", 0));

        splitNode.onProcess(send);
        splitNode.onProcess(warnMsg);

        assertEquals(send, connection1.poll());
        assertEquals(warnMsg, connection2.poll());
    }

    @Test
    @DisplayName("경계값 처리")
    void success_boundary() throws InterruptedException {
        Message send = new Message(Map.of("tick", 3));
        splitNode.onProcess(send);
        assertEquals(send, connection1.poll());
        assertEquals(0, connection2.getBufferSize());
    }

}