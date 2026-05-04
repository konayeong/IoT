package com.fbp.engine.core;

import com.fbp.engine.node.AbstractNode;
import lombok.Getter;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// (외부 통신) 비동기적
public abstract class ProtocolNode extends AbstractNode {
    private final Map<String, Object> config; // 호스트, 포트, 인증 정보 등
    @Getter
    private ConnectionState connectionState;
    private final long reconnectIntervalMs;

    // 스케줄러
    private ScheduledExecutorService scheduler;
    private final int maxRetryCnt;
    private int currentRetryCnt = 0;

    protected ProtocolNode(String id, Map<String, Object> config) {
        super(id);
        this.config = config;
        this.connectionState = ConnectionState.DISCONNECTED;

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        Object intervalMs = config.get("intervalMs");
        this.reconnectIntervalMs = intervalMs instanceof Number ? ((Number) intervalMs).longValue() : 5000L;

        Object maxRetry = config.get("maxRetry");
        this.maxRetryCnt = maxRetry instanceof Number ? ((Number) maxRetry).intValue() : 10;
    }

    @Override
    public void initialize() {
        connectionState = ConnectionState.CONNECTING;
        try{
            connect();
            connectionState = ConnectionState.CONNECTED;
            currentRetryCnt = 0;
        }catch (Exception e) {
            connectionState = ConnectionState.ERROR;
            startReconnectSchedule(); // 재연결 스케줄러
        }
    }

    @Override
    public void shutdown() {
        // 재연결 스케줄러 중지
        if(scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        try {
            disconnect();
        }catch (Exception ignored) {}

        connectionState = ConnectionState.DISCONNECTED;
    }

    private void startReconnectSchedule() {
        if (scheduler.isShutdown() || currentRetryCnt >= maxRetryCnt) {
            return;
        }

        scheduler.schedule(this::reconnect, reconnectIntervalMs, TimeUnit.MILLISECONDS);
    }

    public void reconnect() {
        if (connectionState == ConnectionState.CONNECTED) return;

        currentRetryCnt++;
        connectionState = ConnectionState.CONNECTING;

        try {
            connect();
            connectionState = ConnectionState.CONNECTED;
            currentRetryCnt = 0; // 성공 시 초기화
        } catch (Exception e) {
            connectionState = ConnectionState.ERROR;

            // 최대 횟수에 도달하지 않았다면 다음 재시도 예약
            if (currentRetryCnt < maxRetryCnt) {
                startReconnectSchedule();
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
