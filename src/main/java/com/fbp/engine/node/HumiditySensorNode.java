package com.fbp.engine.node;

import com.fbp.engine.message.Message;

import java.util.HashMap;
import java.util.Map;

public class HumiditySensorNode extends AbstractNode{
    private final double min;
    private final double max;

    public HumiditySensorNode(String id, double min, double max) {
        super(id);
        this.min = min;
        this.max = max;
        addInputPort("trigger");
        addOutputPort("out");
    }

    @Override
    public void onProcess(Message message) {
        double humidity = Math.round((min + Math.random() * (max - min)) * 10) / 10.0;

        Map<String, Object> map = new HashMap<>();
        map.put("sensorId", getId());
        map.put("humidity", humidity);
        map.put("unit", "%");
        map.put("timestamp", System.currentTimeMillis());

        send("out", new Message(map));
    }
}
