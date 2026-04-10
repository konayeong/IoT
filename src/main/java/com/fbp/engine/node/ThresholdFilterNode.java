package com.fbp.engine.node;

import com.fbp.engine.message.Message;

public class ThresholdFilterNode extends AbstractNode{

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

        double value = message.get(fieldName);

        if(value > threshold) {
            send("alert", message);
        }else {
            send("normal", message);
        }
    }
}
