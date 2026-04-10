package com.fbp.engine.runner;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.*;

// 최종 종합
public class App {
    public static void main(String[] args) throws InterruptedException {
        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("final-flow");

        // 노드 추가
        flow.addNode(new TimerNode("timer", 1000))
            .addNode(new TemperatureSensorNode("sensor", 15, 45))
            .addNode(new ThresholdFilterNode("threshold", "temperature", 30))
            .addNode(new AlertNode("alertN"))
            .addNode(new LogNode("log"))
            .addNode(new FileWriterNode("file", "final.txt"));

        // 연결
        flow.connect("timer", "out", "sensor", "trigger")
            .connect("sensor", "out", "threshold", "in")
            .connect("threshold", "alert", "alertN", "in")
            .connect("threshold", "normal", "log", "in")
            .connect("log", "out", "file", "in");

        engine.register(flow); // flowEngine에 flow 등록

        engine.startFlow(flow.getId()); // 플로우 시작

        Thread.sleep(10000); // 10초

        engine.shutdown(); // 종료
    }
}