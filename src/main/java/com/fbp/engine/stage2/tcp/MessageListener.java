package com.fbp.engine.stage2.tcp;

// 외부 시스템 → Listener → Message → OutputPort → FBP 엔진
public interface MessageListener {
    void onMessage(String topic, byte[] payload);
    void onConnectionLost(Throwable cause);
}

