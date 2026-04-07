package com.fbp.engine.core.impl;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.message.Message;

import java.util.ArrayList;
import java.util.List;

public class DefaultOutputPort implements OutputPort {
    private final String name;
    private final List<Connection> connectionList = new ArrayList<>();

    public DefaultOutputPort(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void connect(Connection connection) {
        connectionList.add(connection);
    }

    @Override
    public void send(Message message) {
        for(Connection conn : connectionList) {
            conn.deliver(message);
        }
    }
}
