package com.fbp.engine.node.out;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;
import java.util.Map;

public class GeneratorNode extends AbstractNode {

    public GeneratorNode(String id) {
        super(id);
        addOutputPort("out");
    }

    public void generate(String key, Object value) {
        Message message = new Message(Map.of(key, value));
        this.getOutputPort("out").send(message);
    }

    @Override
    protected void onProcess(Message message) {
        this.getOutputPort("out").send(message);
    }
}