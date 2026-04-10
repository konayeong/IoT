package com.fbp.engine.runner.demo;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.*;

// 온도 모니터링 플로우 + 습도 모니터링 플로우
public class Step9 {
    public static void run() throws InterruptedException {
        FlowEngine engine = new FlowEngine();
        Flow temFlow = new Flow("temperature-monitoring");
        Flow humFlow = new Flow("humidity-monitoring");

        temFlow.addNode(new TimerNode("timer", 1000))
                .addNode(new TemperatureSensorNode("temperature", 15, 45))
                .addNode(new ThresholdFilterNode("threshold", "temperature", 30))
                .addNode(new AlertNode("alert"))
                .addNode(new FileWriterNode("file", "temperature.txt"))
                .connect("timer", "out", "temperature", "trigger")
                .connect("temperature", "out", "threshold", "in")
                .connect("threshold", "alert", "alert", "in")
                .connect("threshold", "normal", "file", "in");

        humFlow.addNode(new TimerNode("timer", 500))
                .addNode(new HumiditySensorNode("humidity", 30, 90))
                .addNode(new ThresholdFilterNode("threshold", "humidity", 70))
                .addNode(new AlertNode("alert"))
                .addNode(new LogNode("log"))
                .connect("timer", "out", "humidity", "trigger")
                .connect("humidity", "out", "threshold", "in")
                .connect("threshold", "alert", "alert", "in")
                .connect("threshold", "normal", "log", "in");

        engine.register(temFlow);
        engine.register(humFlow);

        engine.startFlow(temFlow.getId());
        engine.startFlow(humFlow.getId());

        Thread.sleep(10000);

        engine.shutdown();
    }
}
