package com.fbp.engine.core;

import com.fbp.engine.message.Message;

public interface InputPort {
    String getName();
    void receive(Message message); // Connection으로부터 메시지 수신
}
