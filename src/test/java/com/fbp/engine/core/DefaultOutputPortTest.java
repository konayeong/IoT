package com.fbp.engine.core.impl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.DefaultOutputPort;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;

class DefaultOutputPortTest {
    private DefaultOutputPort outputPort;
    private Message message;

    @BeforeEach
    void setUp() {
        outputPort = new DefaultOutputPort("out");
        message = new Message(Map.of("temperature", 25.0));
    }

    @Test
    @DisplayName("단일 Connection 전달")
    void single_connection() {
        // 	send()하면 연결된 Connection에 메시지가 전달됨
        Connection connection = new Connection("conn-1");
        outputPort.connect(connection);
        outputPort.send(message);

        Assertions.assertEquals(1, connection.getBufferSize());
    }

    @Test
    @DisplayName("다중 Connection 전달 (1:N)")
    void multi_connection() {
        // 2개의 Connection을 연결하고 send()하면 양쪽 모두 메시지를 수신
        Connection connection1 = new Connection("conn-1");
        Connection connection2 = new Connection("conn-2");
        outputPort.connect(connection1);
        outputPort.connect(connection2);

        outputPort.send(message);

        Assertions.assertEquals(1, connection1.getBufferSize());
        Assertions.assertEquals(1, connection2.getBufferSize());
    }

    @Test
    @DisplayName("Connection 미연결 시")
    void nonConnection() {
        // connect()하지 않고 send()해도 예외가 발생하지 않음
        Assertions.assertDoesNotThrow(() -> outputPort.send(message));
    }
}