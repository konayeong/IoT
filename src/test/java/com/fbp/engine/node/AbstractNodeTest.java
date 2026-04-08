package com.fbp.engine.node;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AbstractNodeTest {

    static class TestNode extends AbstractNode {
        boolean processed = false;

        TestNode(String id) {
            super(id);
        }

        @Override
        protected void onProcess(Message message) {
            processed = true;
        }
    }

    private TestNode testNode;

    @BeforeEach
    void setUp() {
        testNode = new TestNode("test");
    }

    @Test
    @DisplayName("getId 반환")
    void getId() {
        assertEquals("test", testNode.getId());
    }

    @Test
    @DisplayName("addInputPort 등록")
    void addInputPort() {
        testNode.addInputPort("in");
        assertNotNull(testNode.getInputPort("in"));
    }

    @Test
    @DisplayName("addOutputPort 등록")
    void addOutputPort() {
        testNode.addOutputPort("out");
        assertNotNull(testNode.getOutputPort("out"));
    }

    @Test
    @DisplayName("미등록 포트 조회")
    void getInputPort_notExists() {
        assertNull(testNode.getInputPort("not"));
    }

    @Test
    @DisplayName("process -> onProcess 호출")
    void process_onProcess() {
        testNode.process(new Message(Map.of()));
        assertTrue(testNode.processed);
    }

    @Test
    @DisplayName("send로 메시지 전달")
    void send() {
        TestNode receiver = new TestNode("receiver");

        testNode.addOutputPort("out");
        receiver.addInputPort("in");

        Connection conn = new Connection("conn");
        conn.setTarget(receiver.getInputPort("in"));
        testNode.getOutputPort("out").connect(conn);

        Message msg = new Message(Map.of("test", "value"));
        testNode.send("out", msg);

        Message received = conn.poll();
        assertEquals(msg.getPayload(), received.getPayload());

    }
}