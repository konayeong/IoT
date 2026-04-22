package com.fbp.engine.node;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.utils.FilterNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class FilterNodeTest {

    private FilterNode node;
    private Connection connection;

    @BeforeEach
    void setUp() {
        node = new FilterNode("filter-1", "temperature", 20);
        connection = new Connection("conn");
        node.getOutputPort("out").connect(connection);
    }
    @Test
    @DisplayName("조건 만족 시 send 호출")
    void process_success() {
        // threshold 이상인 값을 가진 메시지가 OutputPort로 전달됨
        Message message = new Message(Map.of("temperature", 20.5));
        node.process(message);

        Message result = connection.poll();
        assertNotNull(result);
        assertEquals(20.5, result.getPayload().get("temperature"));
    }

    @Test
    @DisplayName("조건 미달 시 차단")
    void process_failed() {
        // threshold 미만인 값을 가진 메시지가 OutputPort로 전달되지 않음
        Message message = new Message(Map.of("temperature", 19));
        node.process(message);

        assertTrue(connection.getBufferSize() == 0);
    }

    @Test
    @DisplayName("경계값 처리")
    void process_equals() {
        // threshold와 정확히 같은 값의 동작 확인 (이상 조건이므로 통과)
        Message message = new Message(Map.of("temperature", 20));
        node.process(message);

        Message result = connection.poll();
        assertNotNull(result);
        assertEquals(20, result.getPayload().get("temperature"));
    }

    @Test
    @DisplayName("키 없는 메시지")
    void notExists_key() {
        // 필터링 대상 키가 없는 메시지가 들어왔을 때 예외 없이 처리됨
        Message message = new Message(Map.of());

        Assertions.assertDoesNotThrow(() -> node.process(message));
    }

    @Test
    @DisplayName("포트 구성 확인")
    void getPort() {
        assertNotNull(node.getInputPort("in"));
        assertNotNull(node.getOutputPort("out"));
    }
}