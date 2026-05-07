//package com.fbp.engine.stage2.runner;
//
//import com.fbp.engine.core.Flow;
//import com.fbp.engine.core.FlowEngine;
//import com.fbp.engine.rule.RuleNode;
//import com.fbp.engine.node.modbus.ModbusWriterNode;
//import com.fbp.engine.node.mqtt.MqttPublisherNode;
//import com.fbp.engine.node.mqtt.MqttSubScriberNode;
//import com.fbp.engine.node.utils.LogNode;
//import java.util.Map;
//
//public class Step4_4 {
//    public static void main(String[] args) {
//        MqttSubScriberNode sub = new MqttSubScriberNode("sub", Map.of(
//                "brokerUrl", "tcp://localhost:1883",
//                "clientId", "sub-client",
//                "topic", "sensor/temp",
//                "qos", 1
//        ));
//
//        RuleNode rule = new RuleNode("rule", message -> {
//            Object tempObj = message.getPayload().get("temperature");
//
//            if (tempObj == null) return false;
//
//            double temp = Double.parseDouble(tempObj.toString());
//            return temp > 30;
//        });
//
//        MqttPublisherNode pub = new MqttPublisherNode("pub", Map.of(
//                "brokerUrl", "tcp://localhost:1883",
//                "clientId", "pub-client",
//                "topic", "alert/topic",
//                "qos", 1
//        ));
//
//        ModbusWriterNode modbus = new ModbusWriterNode("modbus", Map.of(
//                "host", "localhost",
//                "port", 5020,
//                "slaveId", 1,
//                "registerAddress", 0,
//                "valueField", "temperature",
//                "scale", 1
//        ));
//
//        LogNode log = new LogNode("log");
//
//        FlowEngine engine = new FlowEngine();
//        Flow flow = new Flow("flow");
//
//        flow.addNode(sub)
//            .addNode(rule)
//            .addNode(pub)
//            .addNode(modbus)
//            .addNode(log);
//
//        flow.connect("sub", "out", "rule", "in")
//            .connect("rule", "match", "pub", "in")
//            .connect("rule", "match", "modbus", "in")
//            .connect("rule", "mismatch", "log", "in");
//
//        engine.register(flow);
//        engine.startFlow("flow");
//        System.out.println("=== 통합 플로우 실행 시작 ===");
//
////        engine.shutdown();
//    }
//}
