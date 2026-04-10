package com.fbp.engine.runner.demo.step8;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.FilterNode;
import com.fbp.engine.node.LogNode;
import com.fbp.engine.node.PrintNode;
import com.fbp.engine.node.TimerNode;

public class CLIMain {
    public static void run() {
        FlowEngine engine = new FlowEngine();

        Flow flow = new Flow("monitoring");
        flow.addNode(new TimerNode("timer", 500))
            .addNode(new LogNode("log"))
            .addNode(new FilterNode("filter", "tick", 3))
            .addNode(new PrintNode("print"))
            .connect("timer", "out", "log", "in")
            .connect("log", "out", "filter", "in")
            .connect("filter", "out", "print", "in");

        engine.register(flow);

        engine.runCLI();
    }
}
