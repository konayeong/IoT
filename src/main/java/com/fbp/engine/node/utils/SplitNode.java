package com.fbp.engine.node.utils;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

public class SplitNode extends AbstractNode {

    private final String key;
    private final double threshold;

    public SplitNode(String id, String key, double threshold) {
        super(id);
        this.key = key;
        this.threshold = threshold;

        addInputPort("in");
        addOutputPort("match");
        addOutputPort("mismatch");
    }

    @Override
    protected void onProcess(Message message) {
        if(!message.hasKey(key)){
            return;
        }

        Number value = message.get(key);
        double real = value.doubleValue();

        if(real < threshold) {
            send("mismatch", message);
        }else {
            send("match", message);
        }
    }
}
