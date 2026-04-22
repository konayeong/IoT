package com.fbp.engine.core;

import com.fbp.engine.node.AbstractNode;
import lombok.Getter;
import java.io.IOException;
import java.util.Map;

public abstract class ProtocolNode extends AbstractNode {
    private final Map<String, Object> config;
    @Getter
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private final long reconnectIntervalMs = 5000;

    protected ProtocolNode(String id, Map<String, Object> config) {
        super(id);
        this.config = config;
    }

    @Override
    public void initialize() {
        connectionState = ConnectionState.CONNECTING;
        try{
            connect();
            connectionState = ConnectionState.CONNECTED;
        }catch (Exception e) {
            connectionState = ConnectionState.ERROR;
            reconnect();
        }
    }

    @Override
    public void shutdown() {
        try {
            disconnect();
        }catch (Exception ignored) {}

        connectionState = ConnectionState.DISCONNECTED;
    }

    public void reconnect() {
        int maxRetryCount = (int) config.getOrDefault("maxRetry", 10);
        int retry = 0;

        while (retry < maxRetryCount && connectionState != ConnectionState.CONNECTED) {
            try {
                Thread.sleep(reconnectIntervalMs);

                connectionState = ConnectionState.CONNECTING;
                connect();

                connectionState = ConnectionState.CONNECTED;
                return;
            } catch (Exception e) {
                retry++;
                connectionState = ConnectionState.ERROR;
            }
        }

    }

    public Object getConfig(String key) {
        return config.get(key);
    }

    public boolean isConnected() {
        return connectionState == ConnectionState.CONNECTED;
    }

    protected abstract void connect() throws Exception;
    protected abstract void disconnect() throws Exception;
}
