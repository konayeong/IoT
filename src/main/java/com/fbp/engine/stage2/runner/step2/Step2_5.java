package com.fbp.engine.stage2.runner.step2;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.in.PrintNode;
import com.fbp.engine.node.mqtt.MqttSubScriberNode;

import java.util.Map;

public class Step2_5 {
    public static void main(String[] args) {
        Map<String, Object> config = Map.of(
                "brokerUrl", "tcp://localhost:1883",
                "clientId", "client",
                "topic", "sensor/temp",
                "qos", 1
        );

        MqttSubScriberNode subScriberNode = new MqttSubScriberNode("sub", config);
        PrintNode printNode = new PrintNode("print-1");

        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow");
        flow.addNode(subScriberNode);
        flow.addNode(printNode);

        flow.connect("sub", "out", "print-1", "in");

        engine.register(flow);
        engine.startFlow("flow");
    }
}
