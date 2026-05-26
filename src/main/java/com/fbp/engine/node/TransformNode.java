package com.fbp.engine.node;

import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.message.Message;

import java.util.function.Function;

// 데이터 변환
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
