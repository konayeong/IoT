package com.fbp.engine.node.utils;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

import java.util.function.Function;

// 데이터 변환 수행
public class TransformNode extends AbstractNode {

    private Function<Message, Message> transformer;

    public TransformNode(String id, Function<Message, Message> transformer) {
        super(id);
        addInputPort("in");
        addOutputPort("out");
        this.transformer = transformer;
    }

    @Override
    protected void onProcess(Message message) {
        Message result = transformer.apply(message);
        if(result != null) {
            send("out", result);
        }
    }
}
