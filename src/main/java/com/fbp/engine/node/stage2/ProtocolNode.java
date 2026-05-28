package com.fbp.engine.node.stage2;

import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.core.ConnectionState;
import lombok.Getter;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class ProtocolNode extends AbstractNode {

    protected final Map<String, Object> config;
    protected ConnectionState connectionState = ConnectionState.DISCONNECTED;
    protected final long reconnectIntervalMs;

    private final int maxRetryCount;
    private int currentRetryCount = 0;

    private ScheduledExecutorService scheduler;

    protected ProtocolNode(String id, Map<String, Object> config) {
        super(id);

        this.config = Map.copyOf(config);
        this.reconnectIntervalMs = ((Number) config.getOrDefault("reconnectIntervalMs", 5000L)).longValue();
        this.maxRetryCount = ((Number) config.getOrDefault("maxRetry", 10)).intValue();
    }

    @Override
    public void initialize() {
        attemptConnect();
    }

    @Override
    public void shutdown() {
        stopScheduler();

        try {
            disconnect();
        } catch (Exception ignored) {}

        connectionState = ConnectionState.DISCONNECTED;
    }

    public void reconnect() {
        if (isConnected()) {
            return;
        }

        attemptConnect();
    }

    private void attemptConnect() {
        connectionState = ConnectionState.CONNECTING;

        try {
            connect();
            connectionState = ConnectionState.CONNECTED;
            currentRetryCount = 0;

        } catch (Exception e) {
            connectionState = ConnectionState.ERROR;
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        if (currentRetryCount >= maxRetryCount) {
            return;
        }

        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }

        currentRetryCount++;

        scheduler.schedule(this::reconnect, reconnectIntervalMs, TimeUnit.MILLISECONDS);
    }

    private void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
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