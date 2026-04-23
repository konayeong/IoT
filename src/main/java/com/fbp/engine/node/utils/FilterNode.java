package com.fbp.engine.node.utils;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

// 필요없는 데이터는 제거하고 싶을 때
public class FilterNode extends AbstractNode {
    private final String key;
    private final double threshold;

    public FilterNode(String id, String key, double threshold) {
        super(id);
        this.key = key;
        this.threshold = threshold;
        addInputPort("in");
        addOutputPort("out");
    }

    @Override
    protected void onProcess(Message message) {
        if(!message.hasKey(key)) {
            return;
        }
        Number value = message.get(key);
        double object = value.doubleValue();
        // Stage2-Step2_7 조건 수정
        if(threshold < object) {
            send("out", message);
        }
    }
}
