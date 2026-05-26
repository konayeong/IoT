package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import lombok.Setter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Connection {
    private final String id;
    private final BlockingQueue<Message> buffer;
    @Setter
    private InputPort target;

    public Connection(String id, int size) {
        this.id = id;
        this.buffer = new LinkedBlockingQueue<>();
    }

    public Connection(String id) {
        this(id, 100);
    }

    public void deliver(Message message) {
        try {
            buffer.put(message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(target != null) {
            target.receive(message);
        }
    }

    public Message poll() throws InterruptedException {
        return buffer.take();
    }

    public int getBufferSize() {
        return buffer.size();
    }
}
