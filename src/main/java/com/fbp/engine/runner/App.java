package com.fbp.engine.runner;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.*;
import com.fbp.engine.node.in.FileWriterNode;
import com.fbp.engine.node.out.TimerNode;
import com.fbp.engine.node.utils.ThresholdFilterNode;

public class App {

    public static void main(String[] args) {
        FlowEngine engine = new FlowEngine();

        Flow flow = new Flow("demo");

        flow.addNode(new TimerNode("timer", 1000))
                .addNode(new TemperatureSensorNode("sensor", 15, 45))
                .addNode(new ThresholdFilterNode("threshold", "temperature", 30))
                .addNode(new CollectorNode("alert"))
                .addNode(new CollectorNode("normal"))
                .addNode(new FileWriterNode("file", "result.txt"));

        flow.connect("timer", "out", "sensor", "trigger")
                .connect("sensor", "out", "threshold", "in")
                .connect("threshold", "alert", "alert", "in")
                .connect("threshold", "normal", "normal", "in")
                .connect("normal", "out", "file", "in");

        engine.register(flow);

        engine.runCLI();
    }
}