package com.fbp.engine.runner.demo.step8;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.utils.FilterNode;
import com.fbp.engine.node.utils.LogNode;
import com.fbp.engine.node.in.PrintNode;
import com.fbp.engine.node.out.TimerNode;

public class Step8_2 {
    public static void run() throws InterruptedException {
        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow7");

        LogNode logNode = new LogNode("log");
        FilterNode filterNode = new FilterNode("filter", "tick", 3);
        PrintNode printNode = new PrintNode("print");

        flow.addNode(new TimerNode("timer", 500))
                .addNode(logNode)
                .addNode(filterNode)
                .addNode(printNode);

        // 연결 정의
        flow.connect("timer", "out", "log", "in")
                .connect("log", "out", "filter", "in")
                .connect("filter", "out", "print", "in");

        engine.register(flow);
        engine.startFlow(flow.getId()); // flow7

        Thread.sleep(5000);
        engine.shutdown();
    }
}
