package com.fbp.engine.stage2.node;

import com.fbp.engine.core.ConnectionState;
import com.fbp.engine.core.ProtocolNode;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolNodeTest {
    class TestProtocolNode extends ProtocolNode {
        private boolean shouldFail;

        public TestProtocolNode(String id, Map<String, Object> config, boolean shouldFail) {
            super(id, config);
            this.shouldFail = shouldFail;
        }

        @Override
        protected void connect() throws IOException {
            if (shouldFail) {
                throw new RuntimeException("fail connect");
            }
        }

        @Override
        protected void disconnect() throws IOException {
        }

        @Override
        protected void onProcess(Message message) {
        }
    }

    @Test
    @DisplayName("초기 상태")
    void initialize() {
        Map<String, Object> config = new HashMap<>();
        ProtocolNode node = new TestProtocolNode("node1", config, false);
        assertEquals(ConnectionState.DISCONNECTED, node.getConnectionState());
    }

    @Test
    @DisplayName("config 조회")
    void getConfig() {

        Map<String, Object> config = new HashMap<>();
        config.put("host", "localhost");

        ProtocolNode node = new TestProtocolNode("node1", config, false);

        assertEquals("localhost", node.getConfig("host"));
    }

    @Test
    @DisplayName("initialize -> Connected")
    void initialize_connected() {
        Map<String, Object> config = new HashMap<>();

        ProtocolNode node = new TestProtocolNode("node1", config, false);

        node.initialize();

        assertEquals(ConnectionState.CONNECTED, node.getConnectionState());
    }

    @Test
    @DisplayName("initialize -> 연결 실패 시 상태")
    void initialize_failed_ERROR() {

    }

    @Test
    @DisplayName("shutdown -> DISCONNECTED")
    void shutdown_disconnected() {
        Map<String, Object> config = new HashMap<>();

        ProtocolNode node = new TestProtocolNode("node1", config, false);

        node.initialize();
        node.shutdown();

        assertEquals(ConnectionState.DISCONNECTED, node.getConnectionState());
    }

    @Test
    @DisplayName("isConnected 반환값")
    void isConnected_return() {

        Map<String, Object> config = new HashMap<>();

        ProtocolNode node = new TestProtocolNode("node1", config, false);

        node.initialize();

        if (node.getConnectionState() == ConnectionState.CONNECTED) {
            assertTrue(node.isConnected());
        } else {
            assertFalse(node.isConnected());
        }
    }

    @Test
    @DisplayName("재연결 시도")
    void reconnect() {

    }
}