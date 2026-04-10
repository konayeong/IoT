package com.fbp.engine.node;

import com.fbp.engine.message.Message;

import java.util.HashMap;
import java.util.Map;

public class TemperatureSensorNode extends AbstractNode{

    private final double min;
    private final double max;

    public TemperatureSensorNode(String id, double min, double max) {
        super(id);
        this.min = min;
        this.max = max;
        addInputPort("trigger");
        addOutputPort("out");
    }

    @Override
    public void onProcess(Message message) {
        double temperature = Math.round((min + Math.random() * (max - min)) * 10) / 10.0;

        Map<String, Object> map = new HashMap<>();
        map.put("sensorId", getId());
        map.put("temperature", temperature);
        map.put("unit", "°C");
        map.put("timestamp", System.currentTimeMillis());

        send("out", new Message(map));
    }
}
