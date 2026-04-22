package com.fbp.engine.node.mqtt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MqttPublisherNodeTest {

    private MqttPublisherNode pubNode;

    @BeforeEach
    void setUp() {
        pubNode = new MqttPublisherNode(
                "pub",
                Map.of(
                        "brokerUrl", "tcp://localhost:1883",
                        "clientId", "pub-1",
                        "topic", "alert/temp",
                        "qos", 1
                )
        );
    }

    @Test
    @DisplayName("포트 구성")
    void inputPort() {
        assertNotNull(pubNode.getInputPort("in"));
    }

    @Test
    @DisplayName("초기 상태")
    void init() {
        assertFalse(pubNode.isConnected());
    }

    @Test
    @DisplayName("config 기본 토픽 조회")
    void config_basic_topic() {
        assertEquals("alert/temp", pubNode.getConfig("topic"));
    }

}