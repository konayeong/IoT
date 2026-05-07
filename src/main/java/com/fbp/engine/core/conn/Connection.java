package com.fbp.engine.core.conn;

import com.fbp.engine.message.Message;

public interface Connection {
    void deliver(Message message);
    Message poll() throws InterruptedException;
    int getBufferSize();
}