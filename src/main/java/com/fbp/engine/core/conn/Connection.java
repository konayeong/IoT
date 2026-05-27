package com.fbp.engine.core.conn;

import com.fbp.engine.core.port.InputPort;
import com.fbp.engine.message.Message;

public interface Connection {
    void deliver(Message message);
    Message poll() throws InterruptedException;
    int getBufferSize();
    void close();

    String getId();
    void setTarget(InputPort target);
    InputPort getTarget();
}