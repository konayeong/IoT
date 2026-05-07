package com.fbp.engine.registry;

import com.fbp.engine.core.Node;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NodeFactoryTest {

    /**
     * 테스트용 Node
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
    @DisplayName("정상 생성 (람다 구현)")
    void factory_createsNodeSuccessfully() {
        NodeFactory factory = config -> {
            int value = (int) config.get("value");
            return new TestNode("node", value);
        };

        TestNode node = (TestNode) factory.create(Map.of("value", 5));

        assertEquals(5, node.getValue());
    }

    @Test
    @DisplayName("잘못된 config")
    void factory_invalidConfig_throwsException() {
        NodeFactory factory = config -> {
            if (!config.containsKey("value")) {
                throw new IllegalArgumentException("Missing value");
            }
            return new TestNode("node", (int) config.get("value"));
        };

        assertThrows(IllegalArgumentException.class, () ->
                factory.create(Map.of())
        );
    }
}