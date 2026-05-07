package com.fbp.engine.registry;

import com.fbp.engine.core.Node;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class NodeRegistryTest {

    private NodeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new NodeRegistry();
    }

    /**
     * 테스트용 더미 Node
     */
    static class TestNode implements Node {

        private final String id;
        private final int value;

        public TestNode(String id, int value) {
            this.id = id;
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void process(Message message) {
            // 아무것도 안함 (테스트용)
        }

        @Override
        public void initialize() {
        }

        @Override
        public void shutdown() {
        }
    }

    @Test
    @DisplayName("register + create")
    void registerAndCreate_success() {
        registry.register("TestNode", config -> { // 팩토리 등록
            int value = (int) config.get("value");
            return new TestNode("node", value);
        });

        Node node = registry.create("TestNode", Map.of("value", 10)); // 노드 인스턴스 생성

        assertNotNull(node);
        assertInstanceOf(TestNode.class, node);
        assertEquals(10, ((TestNode) node).getValue());
    }

    @Test
    @DisplayName("미등록 타입 create -> Exception")
    void create_unregisteredType_throwsException() {
        assertThrows(NodeRegistryException.class, () ->
                registry.create("Unknown", Map.of())
        );
    }

    @Test
    @DisplayName("중복 등록 처리 -> Exception")
    void register_duplicate_throwsException() {
        registry.register("TestNode", config -> new TestNode("node1", 1));

        assertThrows(NodeRegistryException.class, () ->
                registry.register("TestNode", config -> new TestNode("node2", 2))
        );
    }

    @Test
    @DisplayName("등록된 타입 목록 반환")
    void getRegisteredTypes_returnsCorrectTypes() {
        registry.register("A", config -> new TestNode("node1", 1));
        registry.register("B", config -> new TestNode("node2", 2));

        Set<String> types = registry.getRegisteredTypes();

        assertEquals(2, types.size());
        assertTrue(types.contains("A"));
        assertTrue(types.contains("B"));
    }

    @Test
    @DisplayName("config 전달")
    void create_passesConfigCorrectly() {
        registry.register("TestNode", config -> {
            int value = (int) config.get("value");
            return new TestNode("node1", value);
        });

        TestNode node = (TestNode) registry.create("TestNode", Map.of("value", 42));

        assertEquals(42, node.getValue());
    }

    @Test
    @DisplayName("isRegistered")
    void isRegistered_worksCorrectly() {
        registry.register("TestNode", config -> new TestNode("node", 1));

        assertTrue(registry.isRegistered("TestNode"));
        assertFalse(registry.isRegistered("Unknown"));
    }

    @Test
    @DisplayName("null 등록 -> Exception")
    void register_nullType_throwsException() {
        assertThrows(NodeRegistryException.class, () ->
                registry.register(null, config -> new TestNode("node", 1))
        );
    }

    @Test
    @DisplayName("빈 문자열 등록 -> Exception")
    void register_blankType_throwsException() {
        assertThrows(NodeRegistryException.class, () ->
                registry.register("   ", config -> new TestNode("node", 1))
        );
    }
}