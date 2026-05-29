package com.fbp.engine.core;

import com.fbp.engine.message.Message;

public interface Connection {
    void deliver(Message message);
    Message poll() throws InterruptedException ;
    int getBufferSize();
    String getId();
    void close();

    InputPort getTarget();
    void setTarget(InputPort target);

}
