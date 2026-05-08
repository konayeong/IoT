//package com.fbp.engine.stage2.runner.step2;
//
//import com.fbp.engine.core.Flow;
//import com.fbp.engine.core.FlowEngine;
//import com.fbp.engine.node.out.GeneratorNode;
//import com.fbp.engine.node.mqtt.MqttPublisherNode;
//
//import java.util.Map;
//
//public class Step2_6 {
//    public static void main(String[] args) {
//        Map<String, Object> config = Map.of(
//                "brokerUrl", "tcp://localhost:1883",
//                "clientId", "client",
//                "topic", "sensor/temp",
//                "qos", 1
//        );
//
//        FlowEngine engine = new FlowEngine();
//        Flow flow = new Flow("flow");
//
//        MqttPublisherNode node = new MqttPublisherNode("pub", config);
//        GeneratorNode generatorNode = new GeneratorNode("gen");
//
//        flow.addNode(node);
//        flow.addNode(generatorNode);
//        flow.connect("gen", "out", "pub", "in");
//
//        engine.register(flow);
//        engine.startFlow("flow");
//
//        generatorNode.generate("value", 35);
//    }
//}
