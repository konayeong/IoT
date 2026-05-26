package com.fbp.engine.node;

import com.fbp.engine.message.Message;
import com.fbp.engine.core.AbstractNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class MergeNode extends AbstractNode {

    private final Queue<Message> pending1 = new LinkedList<>();
    private final Queue<Message> pending2 = new LinkedList<>();

    public MergeNode(String id) {
        super(id);
        addInputPort("in-1");
        addInputPort("in-2");
        addOutputPort("out");
    }

    @Override
    public void onProcess(Message message) {
        String input = message.get("inputPort");

        if("in-1".equals(input)) {
            pending1.offer(message);
        }else if("in-2".equals(input)) {
            pending2.offer(message);
        }

        if(!pending1.isEmpty() && !pending2.isEmpty()) {
            Message m1 = pending1.poll();
            Message m2 = pending2.poll();

            Map<String,Object> merged = new HashMap<>();
            merged.putAll(m1.getPayload());
            merged.putAll(m2.getPayload());

            send("out", new Message(merged));
        }
    }
}
