package com.fbp.engine.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Stage1 노드 : process()만 하면 끝
 * Protocol 노드 : 연결, 재연결, 상태관리, timeout, disconnect 필요
 */
@Slf4j
public abstract class ProtocolNode extends AbstractNode{

    protected final Map<String, Object> config;
    @Getter
    protected ConnectionState connectionState = ConnectionState.DISCONNECTED;;
    protected final long reconnectIntervalMs;
    protected final int maxRetryCnt;

    private int currentRetryCnt = 0;
    private ScheduledExecutorService scheduler;

    protected ProtocolNode(String id, Map<String, Object> config) {
        super(id);
        this.config = config;

        Object interval = config.getOrDefault("reconnectIntervalMs", 5000L);
        this.reconnectIntervalMs = ((Number) interval).longValue();

        Object maxRetry = config.getOrDefault("maxRetry", 10);
        this.maxRetryCnt = ((Number) maxRetry).intValue();
    }

    @Override
    public void initialize() { // 노드 시작 = 외부 연결 시작
        connectionState = ConnectionState.CONNECTING;
        try {
            connect();
            connectionState = ConnectionState.CONNECTED;
            currentRetryCnt = 0;
            log.info("[{}] 연결 성공", getId());
        }catch (Exception e) {
            this.connectionState = ConnectionState.ERROR;
            log.error("[{}] 연결 실패, {}", getId(), e.getMessage());

            startReconnect(); // 재연결 스케줄러 시작
        }
    }

    @Override
    public void shutdown() {
        stopScheduler();// 재연결 스케줄러 중지
        try {
            disconnect();
        } catch (Exception e) {
            log.warn("[{}] disconnect 실패: {}", getId(), e.getMessage());
        }

        connectionState = ConnectionState.DISCONNECTED;
        log.info("[{}] 종료 완료", getId());
    }


    /**
     * 연결 끊김 시 호출
     * reconnectIntervalMs 간격으로 connect 재시도
     * 최대 재시도 횟수는 config에서 읽음(default 10)
     */
    public void reconnect() {
        if (connectionState == ConnectionState.CONNECTED) return;
        if (currentRetryCnt >= maxRetryCnt) {
            log.error("[{}] 최대 재시도 횟수 초과", getId());
            return;
        }

        currentRetryCnt++;
        connectionState = ConnectionState.CONNECTING;

        try {
            connect();
            connectionState = ConnectionState.CONNECTED;
            currentRetryCnt = 0;
            log.info("[{}] 재연결 성공", getId());
        } catch (Exception e) {
            connectionState = ConnectionState.ERROR;
            log.warn("[{}] 재연결 실패 ({}/{}): {}", getId(), currentRetryCnt, maxRetryCnt, e.getMessage());

            startReconnect();
        }
    }

    public Object getConfig(String key) {
        return config.get(key);
    }

    public boolean isConnected() {
        return connectionState == ConnectionState.CONNECTED;
    }

    private void startReconnect() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }

        scheduler.schedule(this::reconnect, reconnectIntervalMs, TimeUnit.MILLISECONDS); // 루프가 아니라 1회 예약 반복 구조
    }

    private void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    protected abstract void connect() throws Exception;
    protected abstract void disconnect() throws Exception;
}
