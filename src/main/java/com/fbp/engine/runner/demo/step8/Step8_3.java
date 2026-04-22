package com.fbp.engine.runner.demo.step8;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.in.PrintNode;
import com.fbp.engine.node.out.TimerNode;

public class Step8_3 {
    public static void run() throws InterruptedException {
        FlowEngine engine = new FlowEngine();

        Flow flowA = new Flow("flowA");
        Flow flowB = new Flow("flowB");

        // flowA
        TimerNode timerNodeA = new TimerNode("timerA", 500);
        PrintNode printNodeA = new PrintNode("printA");

        flowA.addNode(timerNodeA)
            .addNode(printNodeA)
            .connect("timerA", "out", "printA", "in");

        // flow B
        TimerNode timerNodeB = new TimerNode("timerB", 1000);
        PrintNode printNodeB = new PrintNode("printB");

        flowB.addNode(timerNodeB)
            .addNode(printNodeB)
            .connect("timerB", "out", "printB", "in");


        engine.register(flowA);
        engine.register(flowB);

        engine.startFlow(flowA.getId());
        engine.startFlow(flowB.getId());

        Thread.sleep(5000);

        engine.shutdown();
    }
}
