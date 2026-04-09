package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import lombok.Getter;
import lombok.Setter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Connection {
    @Getter
    private final String id;
    private final BlockingQueue<Message> buffer;
    @Setter
    private InputPort target;

    public Connection(String id) {
        this(id, 100);
    }

    public Connection(String id, int capacity) {
        this.id = id;
        buffer = new LinkedBlockingQueue<>(capacity);
    }

    public void deliver(Message message) {
        try {
            buffer.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Message poll() {
        try {
            return buffer.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public int getBufferSize() {
        return buffer.size();
    }

}
