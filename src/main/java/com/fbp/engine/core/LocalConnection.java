package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import lombok.Getter;
import lombok.Setter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// JVM 내부 Queue 기반 연결
public class LocalConnection implements Connection {
    private final String id;
    private final BlockingQueue<Message> buffer;
    @Getter @Setter
    private InputPort target;

    public LocalConnection(String id, int size) {
        this.id = id;
        this.buffer = new LinkedBlockingQueue<>(size);
    }

    public LocalConnection(String id) {
        this(id, 100);
    }

    @Override
    public void deliver(Message message) {
        try {
            buffer.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message poll() throws InterruptedException {
        return buffer.take();
    }

    @Override
    public int getBufferSize() {
        return buffer.size();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void close() {
        buffer.clear();
    }
}
