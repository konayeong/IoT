package com.fbp.engine.node.modbus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ModbusReaderNodeTest {

    private ModbusReaderNode readerNode;
    private Map<String, Object> config;
    @BeforeEach
    void setUp() {
        config = Map.of(
                "host","localhost",
                "port", 5020,
                "slaveId", 0,
                "startAddress", 0,
                "count", 3
        );
        readerNode = new ModbusReaderNode("read", config);
    }

    @Test
    @DisplayName("포트 구성")
    void addPort() {
        assertNotNull(readerNode.getInputPort("trigger"));
        assertNotNull(readerNode.getOutputPort("out"));
        assertNotNull(readerNode.getOutputPort("error"));
    }

    @Test
    @DisplayName("초기 상태")
    void initConnected() {
        assertFalse(readerNode.isConnected());
    }

    @Test
    @DisplayName("config 확인")
    void config() {
        assertEquals("localhost", readerNode.getConfig("host"));
        assertEquals(5020, readerNode.getConfig("port"));
        assertEquals(0, readerNode.getConfig("slaveId"));
    }
}