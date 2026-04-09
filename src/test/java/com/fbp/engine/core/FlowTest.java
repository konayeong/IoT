package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlowTest {
    private class TestNode extends AbstractNode {
        @Getter
        private boolean init = false;
        @Getter
        private boolean shut = false;

        public TestNode(String id) {
            super(id);
            addInputPort("in");
            addOutputPort("out");
        }

        @Override
        protected void onProcess(Message message) {}

        @Override
        public void initialize() {
            init = true;
        }

        @Override
        public void shutdown() {
            shut = true;
        }
    }

    private Flow flow;

    @BeforeEach
    void setUp() {
        flow = new Flow("Test Flow");
    }

    @Test
    @DisplayName("노드 등록")
    void addNode() {
        TestNode testNode = new TestNode("test");
        flow.addNode(testNode);
        assertTrue(flow.getNodes().containsKey(testNode.getId()));
        assertTrue(flow.getNodes().containsValue(testNode));
    }

    @Test
    @DisplayName("메서드 체이닝")
    void method_chaining() {
        assertDoesNotThrow(() ->
                flow.addNode(new TestNode("a"))
                    .addNode(new TestNode("b"))
                    .connect("a", "out", "b", "in")
        );
    }

    @Nested
    @DisplayName("connect() 테스트")
    class Connect {
        @BeforeEach
        void setUp() {
            flow.addNode(new TestNode("a"))
                .addNode(new TestNode("b"));
        }

        @Test
        @DisplayName("정상 연결")
        void connect() {
            flow.connect("a", "out", "b", "in");

            assertEquals(1, flow.getConnections().size());
        }

        @Test
        @DisplayName("존재하지 않는 소스 노드 ID")
        void sourceId_notExists() {
            assertThrows(IllegalArgumentException.class, () -> flow.connect("not", "out", "b", "in"));
        }

        @Test
        @DisplayName("존재하지 않는 대상 노드 ID")
        void targetID_notExists() {
            assertThrows(IllegalArgumentException.class, () -> flow.connect("a", "out", "not", "in"));
        }

        @Test
        @DisplayName("존재하지 않는 소스 포트")
        void sourcePort_notExists() {
            assertThrows(IllegalArgumentException.class, () -> flow.connect("a", "not", "b", "in"));
        }

        @Test
        @DisplayName("존재하지 않는 대상 포트")
        void targetPort_notExists() {
            assertThrows(IllegalArgumentException.class, () -> flow.connect("a", "out", "b", "not"));

        }
    }

    @Test
    @DisplayName("validate - 빈 Flow")
    void validate_empty_flow() {
        assertTrue(flow.validate().contains("등록된 노드가 없습니다."));
    }

    @Test
    @DisplayName("validate - 정상 Flow")
    void validate_normal_flow() {
        flow.addNode(new TestNode("a"))
            .addNode(new TestNode("b"));
        assertTrue(flow.validate().isEmpty());
    }

    @Test
    @DisplayName("initialize - 전체 호출")
    void initialize_all() {
        TestNode t1 = new TestNode("A");
        TestNode t2 = new TestNode("B");
        flow.addNode(t1).addNode(t2);

        flow.initialize();

        assertTrue(t1.isInit());
        assertTrue(t2.isInit());
    }

    @Test
    @DisplayName("shutdown - 전체 호출")
    void shutdown_all() {
        TestNode t1 = new TestNode("A");
        TestNode t2 = new TestNode("B");
        flow.addNode(t1).addNode(t2);

        flow.shutdown();

        assertTrue(t1.isShut());
        assertTrue(t2.isShut());
    }

    @Test
    @DisplayName("순환 참조 탐지")
    void hasCycle() {
        TestNode t1 = new TestNode("A");
        TestNode t2 = new TestNode("B");
        TestNode t3 = new TestNode("C");

        flow.addNode(t1).addNode(t2).addNode(t3);
        flow.connect("A", "out", "B", "in")
            .connect("B", "out", "C", "in")
            .connect("C", "out", "A", "in");

        assertTrue(flow.validate().contains("순환 참조 발생"));
    }
}