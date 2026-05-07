package com.fbp.engine.core.serializer;

import com.fbp.engine.message.Message;

public interface MessageSerializer {
    byte[] serialize(Message message);
    Message deserialize(byte[] payload);
}