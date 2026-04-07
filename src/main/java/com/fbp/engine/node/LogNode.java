package com.fbp.engine.node;

import com.fbp.engine.message.Message;

public class LogNode extends AbstractNode{

    public LogNode(String id) {
        super(id);
        addInputPort("in");
        addOutputPort("out");
    }

    @Override
    protected void onProcess(Message message) {

    }
}
