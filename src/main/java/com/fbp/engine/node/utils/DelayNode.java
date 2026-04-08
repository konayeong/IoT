package com.fbp.engine.node.utils;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

public class DelayNode extends AbstractNode {
    private long delayMs;

    public DelayNode(String id, long delayMS) {
        super(id);
        this.delayMs = delayMS;
        addInputPort("in");
        addOutputPort("out");
    }

    @Override
    protected void onProcess(Message message)  {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        send("out", message);
    }
}
