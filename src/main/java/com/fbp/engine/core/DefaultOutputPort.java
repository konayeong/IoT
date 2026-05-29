package com.fbp.engine.core;

import com.fbp.engine.message.Message;

import java.util.ArrayList;
import java.util.List;

public class DefaultOutputPort implements OutputPort {
    private final String name;
    private final List<Connection> connectionList = new ArrayList<>(); // 1:N 전송

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

    @Override
    public void disconnect(Connection connection) {
        connectionList.remove(connection);
    }
}