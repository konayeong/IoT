//package com.fbp.engine.runner.stage1;
//
//import com.fbp.engine.core.Flow;
//import com.fbp.engine.core.FlowEngine;
//import com.fbp.engine.node.*;
//
///**
// * 과제 10-5 (최종 종합)
// * [TimerNode(1초)] → [TemperatureSensorNode(15~45도)]
// *                          ↓
// *                  [ThresholdFilterNode(30도)]
// *                    ↓alert           ↓normal
// *               [AlertNode]      [LogNode] → [FileWriterNode]
// */
//public class Step10Final {
//    public static void main(String[] args) {
//        FlowEngine engine = new FlowEngine();
//
//        Flow flow = new Flow("stage1-final");
//
//        flow.addNode(new TimerNode("timer", 1000))
//                .addNode(new TemperatureSensorNode("sensor", 15, 45))
//                .addNode(new ThresholdFilterNode("threshold", "temperature", 30))
//                .addNode(new AlertNode("alert"))
//                .addNode(new LogNode("log"))
//                .addNode(new FileWriterNode("file", "stage1.txt"));
//
//        flow.connect("timer", "out", "sensor", "trigger")
//                .connect("sensor", "out", "threshold", "in")
//                .connect("threshold", "alert", "alert", "in")
//                .connect("threshold", "normal", "log", "in")
//                .connect("log", "out", "file", "in");
//
//        engine.register(flow);
//
//        engine.runCLI();
//    }
//}
