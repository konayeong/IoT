package com.fbp.engine.runner.demo.step8;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.utils.FilterNode;
import com.fbp.engine.node.utils.LogNode;
import com.fbp.engine.node.in.PrintNode;
import com.fbp.engine.node.out.TimerNode;

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
