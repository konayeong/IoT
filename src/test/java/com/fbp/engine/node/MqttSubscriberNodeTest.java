package com.fbp.engine.node;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MqttSubscriberNodeTest {
    private MqttSubscriberNode subNode;

    @BeforeEach
    void setUp() {
        subNode = new MqttSubscriberNode(
                "sub",
                Map.of(
                        "brokerUrl", "tcp://localhost:1883",
                        "clientId", "sub-1",
                        "topic", "sensor/temp",
                        "qos", 1
                )
        );
    }

    @Test
    @DisplayName("포트 구성")
    void port() {
        assertNotNull(subNode.getOutputPort("out"));
    }

    @Test
    @DisplayName("초기 상태")
    void initialize() {
        assertFalse(subNode.isConnected());
    }

    @Test
    @DisplayName("config 조회")
    void config() {
        assertEquals("tcp://localhost:1883", subNode.getConfig("brokerUrl"));
    }

    @Test
    @DisplayName("JSON -> Message 변환")
    void parsePayload() {
        String json = "{\"temperature\":35}";

        Map<String, Object> result = subNode.parsePayload(json);

        assertEquals(35, result.get("temperature"));
    }

    @Test
    @DisplayName("JSON 파싱 실패 처리")
    void json_parsing_failed() {
        String invalid = "wrong";

        Map<String, Object> result = subNode.parsePayload(invalid);

        assertEquals("wrong", result.get("rawPayload"));
    }
}