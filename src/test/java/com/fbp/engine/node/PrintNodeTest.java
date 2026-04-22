package com.fbp.engine.node;

import com.fbp.engine.core.Node;
import com.fbp.engine.message.Message;
import static org.junit.jupiter.api.Assertions.*;

import com.fbp.engine.node.in.PrintNode;
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
    @DisplayName("포트 구성 확인")
    void inputPort() {
        assertNotNull(printNode.getInputPort("in"));
    }

    @Test
    @DisplayName("process 정상 동작")
    void process() {
        assertDoesNotThrow( () -> printNode.process(new Message(Map.of())));
    }

    @Test
    @DisplayName("AbstractNode 상속 확인")
    void extends_abstractNode() {
        assertInstanceOf(AbstractNode.class, printNode);
    }
}