package com.fbp.engine.node;

import com.fbp.engine.core.InputPort;
import com.fbp.engine.core.Node;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.core.impl.DefaultInputPort;
import com.fbp.engine.core.impl.DefaultOutputPort;
import com.fbp.engine.message.Message;
import lombok.Getter;

public class FilterNode implements Node {
    private final String id;
    private final String key;
    private final double threshold;
    @Getter
    private final InputPort inputPort;
    @Getter
    private final OutputPort outputPort;

    public FilterNode(String id, String key, double threshold) {
        this.id = id;
        this.key = key;
        this.threshold = threshold;
        this.inputPort = new DefaultInputPort("in", this);
        this.outputPort = new DefaultOutputPort("out-filter");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void process(Message message) {
        if(!message.hasKey(key)) {
            return;
        }
        Number value = message.get(key);
        double object = value.doubleValue();
        if(threshold <= object) {
           outputPort.send(message);
        }
    }
}
