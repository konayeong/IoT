//package com.fbp.engine.runner.stage2;
//
//import com.fbp.engine.core.Flow;
//import com.fbp.engine.core.FlowEngine;
//import com.fbp.engine.node.FilterNode;
//import com.fbp.engine.node.MqttPublisherNode;
//import com.fbp.engine.node.MqttSubscriberNode;
//import java.util.Map;
//
//// MqttPublisherNode 코드 수정 안하면 오류 남 (topic 지정)
//public class Step2_7 {
//    public static void main(String[] args) {
//        MqttSubscriberNode subNode = new MqttSubscriberNode(
//                "sub",
//                Map.of(
//                        "brokerUrl", "tcp://localhost:1883",
//                        "clientId", "sub-1",
//                        "topic", "sensor/temp",
//                        "qos", 1
//                )
//        );
//
//        FilterNode filterNode = new FilterNode("filter", "temperature", 30);
//        MqttPublisherNode pubNode = new MqttPublisherNode(
//                "pub",
//                Map.of(
//                        "brokerUrl", "tcp://localhost:1883",
//                        "clientId", "pub-1",
//                        "topic", "alert/temp",
//                        "qos", 1
//                )
//        );
//
//        Flow flow = new Flow("flow");
//        flow.addNode(subNode)
//            .addNode(filterNode)
//            .addNode(pubNode);
//        flow.connect("sub", "out", "filter", "in");
//        flow.connect("filter", "out", "pub", "in");
//
//        FlowEngine engine = new FlowEngine();
//        engine.register(flow);
//        engine.startFlow("flow");
//    }
//}