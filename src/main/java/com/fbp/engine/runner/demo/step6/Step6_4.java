package com.fbp.engine.runner.demo.step6;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.in.PrintNode;
import com.fbp.engine.node.out.TimerNode;
import com.fbp.engine.node.utils.SplitNode;
import com.fbp.engine.core.Flow;

// Step 7-3 재구성
public class Step6_4 {
    public static void run() throws InterruptedException {
        Flow flow = new Flow("flow test 2");

        SplitNode splitNode = new SplitNode("split", "tick", 3);
        PrintNode normalPrintNode = new PrintNode("normal");
        PrintNode warnPrintNode = new PrintNode("warn");

        flow.addNode(new TimerNode("timer", 500))
            .addNode(splitNode)
            .addNode(normalPrintNode)
            .addNode(warnPrintNode);

        flow.connect("timer", "out", "split", "in")
            .connect("split", "match", "print", "in")
            .connect("split", "mismatch", "print", "in");

        flow.initialize();

        Connection conn1 = flow.getConnections().get(0);
        Connection conn2 = flow.getConnections().get(1);
        Connection conn3 = flow.getConnections().get(2);


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

        Thread.sleep(3000);

        flow.shutdown();

        split.interrupt();
        normalPrint.interrupt();
        warnPrint.interrupt();

        split.join();
        normalPrint.join();
        warnPrint.join();
    }
}
