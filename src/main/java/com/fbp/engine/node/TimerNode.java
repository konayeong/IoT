package com.fbp.engine.node;

import com.fbp.engine.message.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TimerNode extends AbstractNode {

    private final long intervalMs;
    @Getter
    private int tickCount = 0;
    private ScheduledExecutorService scheduler;

    public TimerNode(String id, long intervalMs) {
        super(id);
        this.intervalMs = intervalMs;
        addOutputPort("out");
    }

    @Override
    public void initialize() {
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("tick", tickCount);
                payload.put("timestamp", System.currentTimeMillis());

                Message message = new Message(payload);

                send("out", message);

                tickCount++;
            } catch (Exception e) {
                log.error("[TimerNode] initialize 중 오류 발생", e);
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        if(scheduler != null) {
            scheduler.shutdown();
        }
    }

    @Override
    protected void onProcess(Message message) {
        // TimerNode는 외부 메시지를 처리하지 않음
    }
}
