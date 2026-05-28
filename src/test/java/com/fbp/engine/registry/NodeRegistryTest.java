package com.fbp.engine.registry;

import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NodeRegistryTest {

    private final NodeRegistry registry = new NodeRegistry();

    // 테스트용 노드
    static class TestNode extends AbstractNode {

        private final Map<String, Object> config;

        protected TestNode(String id, Map<String, Object> config) {
            super(id);
            this.config = config;
        }

        @Override
        protected void onProcess(Message message) {
        }

        public Map<String, Object> getConfig() {
            return config;
        }
    }

    @Test
    @DisplayName("register + create")
    void register_and_create() {

        registry.register(
                "test",
                TestNode::new
        );

        TestNode node = (TestNode) registry.create(
                "test",
                "node1",
                Map.of("key", "value")
        );

        assertNotNull(node);
        assertEquals("node1", node.getId());
    }

    @Test
    @DisplayName("미등록 타입 create")
    void create_unregistered_type() {

        assertThrows(
                NodeRegistryException.class,
                () -> registry.create(
                        "unknown",
                        "node1",
                        Map.of()
                )
        );
    }

    @Test
    @DisplayName("중복 등록 처리")
    void duplicate_register() {

        registry.register(
                "test",
                TestNode::new
        );

        registry.register(
                "test",
                (id, config) -> new TestNode(id, Map.of("override", true))
        );

        TestNode node = (TestNode) registry.create(
                "test",
                "node1",
                Map.of()
        );

        assertEquals(
                true,
                node.getConfig().get("override")
        );
    }

    @Test
    @DisplayName("getRegisteredTypes")
    void get_registered_types() {

        registry.register(
                "type1",
                TestNode::new
        );

        registry.register(
                "type2",
                TestNode::new
        );

        Set<String> types = registry.getRegisteredTypes();

        assertEquals(2, types.size());
        assertTrue(types.contains("type1"));
        assertTrue(types.contains("type2"));
    }

    @Test
    @DisplayName("config 전달")
    void config_passed_correctly() {

        registry.register(
                "test",
                TestNode::new
        );

        Map<String, Object> config = Map.of(
                "host", "localhost",
                "port", 1883
        );

        TestNode node = (TestNode) registry.create(
                "test",
                "node1",
                config
        );

        assertEquals(
                "localhost",
                node.getConfig().get("host")
        );

        assertEquals(
                1883,
                node.getConfig().get("port")
        );
    }

    @Test
    @DisplayName("isRegistered")
    void is_registered() {

        registry.register(
                "test",
                TestNode::new
        );

        assertTrue(
                registry.isRegistered("test")
        );

        assertFalse(
                registry.isRegistered("unknown")
        );
    }

    @Test
    @DisplayName("null 타입명 등록")
    void register_null_type() {

        assertThrows(
                NodeRegistryException.class,
                () -> registry.register(
                        null,
                        TestNode::new
                )
        );
    }

    @Test
    @DisplayName("빈 타입명 등록")
    void register_blank_type() {

        assertThrows(
                NodeRegistryException.class,
                () -> registry.register(
                        " ",
                        TestNode::new
                )
        );
    }

    @Test
    @DisplayName("null 타입명 조회")
    void create_null_type() {

        assertThrows(
                NodeRegistryException.class,
                () -> registry.create(
                        null,
                        "node1",
                        Map.of()
                )
        );
    }

    @Test
    @DisplayName("빈 타입명 조회")
    void create_blank_type() {

        assertThrows(
                NodeRegistryException.class,
                () -> registry.create(
                        "",
                        "node1",
                        Map.of()
                )
        );
    }
}