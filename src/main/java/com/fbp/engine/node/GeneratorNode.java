package com.fbp.engine.node;

import com.fbp.engine.message.Message;
import java.util.Map;

// TODO 이제 필요없나 ?
public class GeneratorNode extends AbstractNode {

    public GeneratorNode(String id) {
        super(id);
        addOutputPort("out");
    }

    @Override
    protected void onProcess(Message message) {

    }

    public void generate(String key, Object value) {
        Message message = new Message(Map.of(key, value));
        send("out", message);
    }
}