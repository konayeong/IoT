package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import lombok.Getter;
import lombok.Setter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Connection {
    @Getter
    private final String id;
    private final BlockingQueue<Message> buffer; // 스레드 안전
    @Setter @Getter
    private InputPort target;

    public Connection(String id) {
        this(id, 100);
    }

    public Connection(String id, int capacity) {
        this.id = id;
        buffer = new LinkedBlockingQueue<>(capacity);
    }

    public void deliver(Message message) { // 생산
        try {
            buffer.put(message); // 큐가 가득 차면 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Message poll() { // 소비
        try {
            return buffer.take(); // 큐가 비면 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public int getBufferSize() {
        return buffer.size();
    }

}
