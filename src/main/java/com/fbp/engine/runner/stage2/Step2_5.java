package com.fbp.engine.runner.stage2;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.MqttSubscriberNode;
import com.fbp.engine.node.PrintNode;

import java.util.Map;

public class Step2_5 {
    public static void main(String[] args) {
        Map<String, Object> config = Map.of(
                "brokerUrl", "tcp://localhost:1883",
                "clientId", "client",
                "topic", "sensor/temp",
                "qos", 1
        );

        MqttSubscriberNode subScriberNode = new MqttSubscriberNode("sub", config);
        PrintNode printNode = new PrintNode("print");

        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow");
        flow.addNode(subScriberNode);
        flow.addNode(printNode);

        flow.connect("sub", "out", "print", "in");

        engine.register(flow);
        engine.startFlow("flow");
    }
}
