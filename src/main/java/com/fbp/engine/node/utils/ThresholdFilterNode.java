package com.fbp.engine.node.utils;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

public class ThresholdFilterNode extends AbstractNode {

    private final String fieldName;
    private final double threshold;

    public ThresholdFilterNode(String id, String fieldName, double threshold) {
        super(id);
        this.fieldName = fieldName;
        this.threshold = threshold;
        addInputPort("in");
        addOutputPort("alert");
        addOutputPort("normal");
    }

    @Override
    public void onProcess(Message message) {
        if(!message.hasKey(fieldName)) {
            return;
        }

        Number num = message.get(fieldName);
        double value = num.doubleValue();

        if(value > threshold) {
            send("alert", message);
        }else {
            send("normal", message);
        }
    }
}
