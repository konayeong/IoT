package com.fbp.engine.node;

import com.fbp.engine.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;


class FilterNodeTest {
    @Test
    @DisplayName("조건 만족 시 통과")
    void process_success() {
        // threshold 이상인 값을 가진 메시지가 OutputPort로 전달됨
        FilterNode node = new FilterNode("filter-1", "temperature", 20);
        Message message = new Message(Map.of("temperature", 20.5));

        node.process(message);

    }

    @Test
    @DisplayName("조건 미달 시 차단")
    void process_failed() {
        // threshold 미만인 값을 가진 메시지가 OutputPort로 전달되지 않음
        FilterNode node = new FilterNode("filter-1", "temperature", 20);
        Message message = new Message(Map.of("temperature", 19));

        node.process(message);
    }

    @Test
    @DisplayName("경계값 처리")
    void process_equals() {
        // threshold와 정확히 같은 값의 동작 확인 (이상 조건이므로 통과)
        FilterNode node = new FilterNode("filter-1", "temperature", 20);
        Message message = new Message(Map.of("temperature", 20));

        node.process(message);
    }

    @Test
    @DisplayName("키 없는 메시지")
    void notExists_key() {
        // 필터링 대상 키가 없는 메시지가 들어왔을 때 예외 없이 처리됨
        FilterNode node = new FilterNode("filter-1", "temperature", 20);
        Message message = new Message(Map.of());

        Assertions.assertDoesNotThrow(() -> node.process(message));
    }
}