package com.fbp.engine.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class Message {
    private final String id;
    private final Map<String, Object> payload; // Map : 데이터 형태가 계속 바뀜
    private final long timestamp;

    @JsonCreator
    public Message(
            @JsonProperty("id") String id,
            @JsonProperty("payload") Map<String, Object> payload,
            @JsonProperty("timestamp") long timestamp
    ) {
        this.id = id;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    // 생성자 : ID 자동 생성, 페이로드 불변 복사, 타임스탬프 자동 기록
    public Message(Map<String, Object> payload) {
        this.id = UUID.randomUUID().toString();
        this.payload = Map.copyOf(payload); // 불변 복사
        this.timestamp = System.currentTimeMillis();
    }

    // payload 값 꺼내기 ( 제네릭 -> 꺼낼 때 매번 캐스팅 안해도 됨 )
    public <T> T get(String key) {
        return (T) payload.get(key);
    }

    @Override
    public String toString() {
        return "Message{id=" + id + ", timestamp=" + timestamp + ", payload=" + payload + "}";
    }

    // 기존 페이로드에 항목을 추가한 새 Message 반환 (원본 불변)
    public Message withEntry(String key, Object value) {
        Map<String, Object> newPayload = new HashMap<>(this.payload);
        newPayload.put(key, value);
        return new Message(newPayload);
    }

    public boolean hasKey(String key) {
        return payload.containsKey(key);
    }

    // 특정 키를 제거한 새 Message 반환
    public Message withoutKey(String key) {
        Map<String, Object> withoutKeyPayload = new HashMap<>(this.payload);
        withoutKeyPayload.remove(key);
        return new Message(withoutKeyPayload);
    }
}