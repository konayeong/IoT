package com.fbp.engine.message;

import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

class MessageTest {

    private Message message;

    @BeforeEach
    void setUp() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("test1", 100);
        payload.put("test2", "value");
        payload.put("temperature", 25.5);

        message = new Message(payload);
    }

    @Test
    @DisplayName("생성 시 ID 자동 할당")
    void construct_id_auto() {
        // getId()가 null이 아니고 빈 문자열이 아님
        Assertions.assertNotNull(message.getId());
        Assertions.assertFalse(message.getId().isEmpty());
    }

    @Test
    @DisplayName("생성 시 timestamp 자동 기록")
    void construct_timestamp_auto() {
        // getTimestamp() > 0
        Assertions.assertTrue(message.getTimestamp() > 0);
    }

    @Test
    @DisplayName("페이로드 조회")
    void getPayload() {
        Assertions.assertEquals(100, (Integer) message.get("test1"));
        Assertions.assertEquals("value", message.get("test2"));
    }

    @Test
    @DisplayName("제네릭 get 타입 캐스팅")
    void type_casting(){
        Assertions.assertEquals(25.5, message.get("temperature"));
    }

    @Test
    @DisplayName("존재하지 않는 키 조회")
    void notExists_key() {
        Assertions.assertNull(message.get("not"));
    }

    @Test
    @DisplayName("페이로드 불변 - 외부 수정 차단")
    void payload_final_1() {
        Map<String, Object> map = message.getPayload();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> map.put("key", "value"));
    }

    @Test
    @DisplayName("페이로드 불변 - 원본 Map 수정 무영향")
    void payload_final_2() {
        message.withoutKey("test1");
        Assertions.assertEquals(100, (Integer) message.get("test1"));
    }

    @DisplayName("withEntry")
    @Nested
    class withEntry {
        private Message newMessage;

        @BeforeEach
        void setUp() {
            newMessage = message.withEntry("test", "value");
        }
        @Test
        @DisplayName("새 겍체 반환")
        void newObject() {
            Assertions.assertNotEquals(message, newMessage);
        }

        @Test
        @DisplayName("withEntry - 원본 불변")
        void origin_final() {
            Assertions.assertEquals(100, (Integer) message.get("test1"));
        }
        @Test
        @DisplayName("withEntry - 새 메시지에 값 존재")
        void newMessage_existsValue() {
            Assertions.assertEquals("value", newMessage.get("test"));
        }
    }

    @DisplayName("hasKey")
    @Nested
    class hasKey {
        @Test
        @DisplayName("존재하는 키")
        void exists() {
            Assertions.assertTrue(message.hasKey("temperature"));
        }

        @Test
        @DisplayName("없는 키")
        void not_exists() {
            Assertions.assertFalse(message.hasKey("not"));
        }
    }

    @DisplayName("withoutKey")
    @Nested
    class withoutKey {
        private Message withoutMsg;

        @BeforeEach
        void setUp() {
            withoutMsg = message.withoutKey("temperature");
        }

        @Test
        @DisplayName("키 제거 확인")
        void remove_success() {
            Assertions.assertFalse(withoutMsg.hasKey("temperature"));
        }

        @Test
        @DisplayName("원본 불변")
        void remove_origin_final() {
            Assertions.assertTrue(message.hasKey("temperature"));
        }
    }

    @Test
    @DisplayName("toString 포맷")
    void toString_format() {
        Assertions.assertNotNull(message.toString());
        Assertions.assertTrue(message.toString().contains(message.getPayload().toString()));
    }
}