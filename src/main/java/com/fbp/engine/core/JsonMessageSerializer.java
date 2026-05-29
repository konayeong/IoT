package com.fbp.engine.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbp.engine.message.Message;

public class JsonMessageSerializer implements MessageSerializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Message message) {
        try {
            return objectMapper.writeValueAsBytes(message);
        } catch (Exception e) {
            throw new RuntimeException("Message serialize 실패", e);
        }
    }

    @Override
    public Message deserialize(byte[] payload) {
        try {
            return objectMapper.readValue(payload, Message.class);
        } catch (Exception e) {
            throw new RuntimeException("Message deserialize 실패", e);
        }
    }
}