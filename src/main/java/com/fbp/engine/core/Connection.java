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

    public Connection(String id, int size) {
        this.id = id;
        this.buffer = new LinkedBlockingQueue<>(size);
    }

    public Connection(String id) {
        this(id, 100);
    }

    public void deliver(Message message) {
        try {
            buffer.put(message);
            if(target != null) {
                target.receive(message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public Message poll() throws InterruptedException {
        return buffer.take();
    }

    public int getBufferSize() {
        return buffer.size();
    }
}
