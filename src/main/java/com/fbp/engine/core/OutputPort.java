package com.fbp.engine.core;

import com.fbp.engine.message.Message;

public interface OutputPort {
    String getName();
    void connect(Connection connection); // Connection 연결
    void send(Message message); // 연결된 모든 Connection으로 메시지 전송
}
