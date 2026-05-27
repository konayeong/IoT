package com.fbp.engine.core.conn;

import com.fbp.engine.core.port.InputPort;
import com.fbp.engine.message.Message;
import lombok.Getter;
import lombok.Setter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// transport 섹션이 없을 때 (BlockingQueue 기반)
public class LocalConnection implements Connection{
    @Getter
    private final String id;
    private final BlockingQueue<Message> buffer; // 스레드 안전
    @Setter @Getter
    private InputPort target;

    public LocalConnection(String id) {
        this(id, 100);
    }

    public LocalConnection(String id, int capacity) {
        this.id = id;
        buffer = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public void deliver(Message message) { // 생산
        try {
            buffer.put(message); // 큐가 가득 차면 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Message poll() throws InterruptedException { // 소비
        return buffer.take(); // 큐가 비면 대기
    }

    @Override
    public int getBufferSize() {
        return buffer.size();
    }

    @Override
    public void close() {
        buffer.clear();
    }

}
