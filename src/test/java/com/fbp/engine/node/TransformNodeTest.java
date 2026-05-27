package com.fbp.engine.node;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TransformNodeTest {

    private TransformNode transformNode;
    private Connection connection;

    @BeforeEach
    void setUp() {
        transformNode = new TransformNode("transform", msg -> {
            Double fahrenheit = msg.get("temperature");
            double celsius = (fahrenheit - 32) * 5.0 / 9.0;
            return msg.withEntry("temperature", celsius);
        });
        connection = new Connection("conn");
        transformNode.getOutputPort("out").connect(connection);
    }

    @Test
    @DisplayName("변환 정상 동작")
    void transformer() throws InterruptedException {
        Message message = new Message(Map.of("temperature", 68.0));
        transformNode.onProcess(message);

        assertEquals(1, connection.getBufferSize());
        assertEquals(20, (Double) connection.poll().get("temperature"));
    }

    @Test
    @DisplayName("null 반환 시 미전달")
    void null_transformer_no() {
        TransformNode node = new TransformNode("transform", msg -> null);
        Connection conn2 = new Connection("conn2");
        node.getOutputPort("out").connect(conn2);

        node.onProcess(new Message(Map.of()));
        assertEquals(0, conn2.getBufferSize());
    }

    @Test
    @DisplayName("원본 메시지 불변")
    void orogin_message() throws InterruptedException {
        Message origin = new Message(Map.of("temperature", 68.0));
        transformNode.onProcess(origin);

        Message result = connection.poll();

        assertEquals(20.0, result.getPayload().get("temperature"));
        assertEquals(68.0, origin.getPayload().get("temperature"));
    }
}