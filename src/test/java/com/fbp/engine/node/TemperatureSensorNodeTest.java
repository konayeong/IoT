package com.fbp.engine.node;

import com.fbp.engine.core.conn.LocalConnection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureSensorNodeTest {

    private TemperatureSensorNode sensor;
    private LocalConnection connection;

    @BeforeEach
    void setUp(){
        connection = new LocalConnection("conn");
        sensor = new TemperatureSensorNode("sensor", 30, 90);
        sensor.getOutputPort("out").connect(connection);
    }

    @Test
    @DisplayName("온도 범위 확인")
    void temperature_range() {
        for(int i=0; i<100; i++) {
            sensor.onProcess(new Message(Map.of()));
            double temper = (double) connection.poll().getPayload().get("temperature");

            assertTrue(temper >= 30 && temper <= 90);
        }
    }

    @Test
    @DisplayName("필수 키 포함")
    void contains_primaryKey() {
        sensor.onProcess(new Message(Map.of()));
        Message msg = connection.poll();
        Assertions.assertAll(
                () -> assertTrue(msg.getPayload().containsKey("sensorId")),
                () -> assertTrue(msg.getPayload().containsKey("temperature")),
                () -> assertTrue(msg.getPayload().containsKey("unit")),
                () -> assertTrue(msg.getPayload().containsKey("timestamp"))
        );
    }

    @Test
    @DisplayName("sensorId 일치")
    void sensorId() {
        sensor.onProcess(new Message(Map.of()));
        Message message = connection.poll();
        assertEquals(sensor.getId(), message.getPayload().get("sensorId"));
    }

    @Test
    @DisplayName("트리거마다 생성")
    void trigger() {
        sensor.getInputPort("trigger").receive(new Message(Map.of()));
        sensor.getInputPort("trigger").receive(new Message(Map.of()));
        sensor.getInputPort("trigger").receive(new Message(Map.of()));

        assertEquals(3, connection.getBufferSize());
    }

}