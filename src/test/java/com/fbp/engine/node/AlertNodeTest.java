package com.fbp.engine.node;

import com.fbp.engine.message.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AlertNodeTest {

    @Test
    @DisplayName("정상 처리")
    void success() {
        AlertNode alertNode = new AlertNode("alert");
        Map<String, Object> map = new HashMap<>();
        map.put("sensorId", "sensor");
        map.put("temperature", 30.3);

        assertDoesNotThrow(() -> alertNode.onProcess(new Message(map)));
    }

    @Test
    @DisplayName("키 누락 시 처리")
    void key_not_exists() {
        AlertNode alertNode = new AlertNode("alert");
        Map<String, Object> map = new HashMap<>();
        map.put("sensorId", "sensor");

        assertDoesNotThrow(() -> alertNode.onProcess(new Message(map)));
    }
}