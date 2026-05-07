package com.fbp.engine.core.port;

import com.fbp.engine.core.conn.LocalConnection;
import com.fbp.engine.message.Message;

public interface OutputPort {
    String getName();
    void connect(LocalConnection connection);
    void send(Message message);
}
