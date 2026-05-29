package com.fbp.engine.node;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.LocalConnection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HumiditySensorNodeTest {

    private HumiditySensorNode sensorNode;
    private Connection conn;

    @BeforeEach
    void setUp() {
        sensorNode = new HumiditySensorNode("sensor", 30, 90);
        conn = new LocalConnection("conn");
        sensorNode.getOutputPort("out").connect(conn);
    }

    @Test
    @DisplayName("습도 범위 확인")
    void humidity_range() throws InterruptedException {
        for(int i=0; i<10; i++) {
            sensorNode.onProcess(new Message(Map.of()));
            double humidity = conn.poll().get("humidity");
            assertTrue(humidity >=30 && humidity <= 90);
        }
    }

    @Test
    @DisplayName("필수 키 포함")
    void contains_primary_key() throws InterruptedException {
        sensorNode.onProcess(new Message(Map.of()));
        Message msg = conn.poll();

        Assertions.assertAll(
                () -> assertTrue(msg.getPayload().containsKey("sensorId")),
                () -> assertTrue(msg.getPayload().containsKey("humidity")),
                () -> assertTrue(msg.getPayload().containsKey("unit"))
        );
    }

    @Test
    @DisplayName("sensorId 일치")
    void sensorId_equals() throws InterruptedException {
        sensorNode.onProcess(new Message(Map.of()));
        Message msg = conn.poll();
        assertEquals(sensorNode.getId(), msg.getPayload().get("sensorId"));
    }

    @Test
    @DisplayName("트리거마다 생성")
    void trigger_new()  {
        sensorNode.getInputPort("trigger").receive(new Message(Map.of()));
        sensorNode.getInputPort("trigger").receive(new Message(Map.of()));
        sensorNode.getInputPort("trigger").receive(new Message(Map.of()));

        assertEquals(3, conn.getBufferSize());
    }
}