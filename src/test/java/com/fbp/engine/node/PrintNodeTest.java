package com.fbp.engine.node;

import com.fbp.engine.core.Node;
import com.fbp.engine.message.Message;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;


class PrintNodeTest {

    private PrintNode printNode;

    @BeforeEach
    void setUp() {
        printNode = new PrintNode("print 1");
    }

    @Test
    @DisplayName("getId 반환")
    void getId() {
        assertEquals("print 1", printNode.getId());
    }

    @Test
    @DisplayName("process 정상 작동")
    void process_success() {
        assertDoesNotThrow(
                () -> printNode.process(new Message(Map.of())));
    }

    @Test
    @DisplayName("Node 인터페이스 구현")
    void node_instance() {
        assertInstanceOf(Node.class, printNode);
    }

    @Test
    @DisplayName("InputPort 조회")
    void inputPort() {
//        assertNotNull(printNode.getInputPort());
    }

    @Test
    @DisplayName("InputPort를 통한 수신")
    void inputPort_receive() {
        // InputPort의 receive()를 호출하면 process()가 실행됨
    }
}