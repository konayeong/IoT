package com.fbp.engine.message;

import lombok.Getter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 노드 간 전달되는 데이터 패킷 - !불변!
@Getter
public class Message {
    private final String id;
    private final Map<String, Object> payload; // Map : 데이터 형태가 계속 바뀜
    private final long timestamp;

    public Message(Map<String, Object> payload) {
        this.id = String.valueOf(UUID.randomUUID());
        this.payload = Map.copyOf(payload); // 불변 복사
        this.timestamp = System.currentTimeMillis();
    }

    // payload 값 꺼내기 ( 제네릭 -> 꺼낼 때 매번 캐스팅 안해도 됨 )
    public <T> T get(String key) {
        return (T) payload.get(key);
    }

    @Override
    public String toString() {
        return payload.toString();
    }

    public Message withEntry(String key, Object value) {
        Map<String, Object> newPayload = new HashMap<>(this.payload);
        newPayload.put(key, value);
        return new Message(newPayload);
    }

    public boolean hasKey(String key) {
        return payload.containsKey(key);
    }

    public Message withoutKey(String key) {
        Map<String, Object> withoutKeyPayload = new HashMap<>(payload);
        withoutKeyPayload.remove(key);
        return new Message(withoutKeyPayload);
    }
}