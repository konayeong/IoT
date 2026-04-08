package com.fbp.engine.runner.demo.step6;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.PrintNode;
import com.fbp.engine.node.TimerNode;
import com.fbp.engine.node.utils.SplitNode;

public class Step6_4 {
    public static void run() {
        TimerNode timerNode = new TimerNode("timer", 500);
        SplitNode splitNode = new SplitNode("filter", "tick", 3);
        PrintNode normalPrintNode = new PrintNode("normal");
        PrintNode warnPrintNode = new PrintNode("warn");

        Connection conn1 = new Connection("conn1");
        Connection conn2 = new Connection("conn2");
        Connection conn3 = new Connection("conn3");

        // connect
        conn1.setTarget(splitNode.getInputPort("in"));
        conn2.setTarget(normalPrintNode.getInputPort("in"));
        conn3.setTarget(warnPrintNode.getInputPort("in"));

        timerNode.getOutputPort("out").connect(conn1);
        splitNode.getOutputPort("match").connect(conn2);
        splitNode.getOutputPort("mismatch").connect(conn3);

        timerNode.initialize();
        splitNode.initialize();
        normalPrintNode.initialize();
        warnPrintNode.initialize();

        Thread split = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                Message message = conn1.poll();
                if(message == null) break;
                splitNode.process(message);
            }
        });

        Thread normalPrint = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = conn2.poll();
                if(message == null) break;
                normalPrintNode.process(message);
            }
        });

        Thread warnPrint = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = conn3.poll();
                if(message == null) break;
                warnPrintNode.process(message);
            }
        });

        split.start();
        normalPrint.start();
        warnPrint.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        timerNode.shutdown();
        splitNode.shutdown();
        normalPrintNode.shutdown();

        split.interrupt();
        normalPrint.interrupt();
        warnPrint.interrupt();

        try {
            split.join();
            normalPrint.join();
            warnPrint.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
