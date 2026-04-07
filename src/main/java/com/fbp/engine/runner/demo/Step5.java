package com.fbp.engine.runner.demo;

import com.fbp.engine.core.Connection;
import com.fbp.engine.node.FilterNode;
import com.fbp.engine.node.PrintNode;
import com.fbp.engine.node.TimerNode;

public class Step5 {
    public static void run() {
        TimerNode timerNode = new TimerNode("timer-1", 500);
        FilterNode filterNode = new FilterNode("filter-1", "tick", 3);
        PrintNode printNode = new PrintNode("print-1");

        // connect
        Connection conn1 = new Connection("conn-1");
        Connection conn2 = new Connection("conn-2");

        conn1.setTarget(filterNode.getInputPort("in"));
        conn2.setTarget(printNode.getInputPort("in"));

        timerNode.getOutputPort("out").connect(conn1);
        filterNode.getOutputPort("out").connect(conn2);

        timerNode.initialize();
        filterNode.initialize();
        printNode.initialize();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        timerNode.shutdown();
        filterNode.shutdown();
        printNode.shutdown();
    }
}
