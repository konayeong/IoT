package com.fbp.engine.core;

import com.fbp.engine.message.Message;

// 모든 노드가 구현할 최소한의 계약
public interface Node {
    String getId();
    void process(Message message); // 메시지를 받아서 처리, 메시지 소비

    void initialize();
    void shutdown();
}
