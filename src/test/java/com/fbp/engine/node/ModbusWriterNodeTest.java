package com.fbp.engine.node;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ModbusWriterNodeTest {

    private ModbusWriterNode writerNode;
    private Map<String, Object> config;

    @BeforeEach
    void setUp() {
        config = Map.of(
                "host","localhost",
                "port", 5020,
                "slaveId", 0,
                "registerAddress", 0,
                "valueField", 1,
                "scale", 1.0
        );
        writerNode = new ModbusWriterNode("write", config);
    }

    @Test
    @DisplayName("포트 구성")
    void port() {
        assertNotNull(writerNode.getInputPort("in"));
        assertNotNull(writerNode.getOutputPort("result"));
    }

    @Test
    @DisplayName("초기 상태")
    void init() {
        assertFalse(writerNode.isConnected());
    }

    @Test
    @DisplayName("config 확인")
    void conifg() {
        assertEquals(0, writerNode.getConfig("registerAddress"));
        assertEquals(1, writerNode.getConfig("valueField"));
        assertEquals(1.0, writerNode.getConfig("scale"));
    }
}