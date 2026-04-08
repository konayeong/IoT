package com.fbp.engine.runner.demo.step6;

import com.fbp.engine.node.PrintNode;
import com.fbp.engine.node.TimerNode;
import com.fbp.engine.node.utils.SplitNode;
import com.fbp.engine.runner.Flow;

// Step 7-3 재구성
public class Step6_4 {
    public static void run() {
        Flow flow = new Flow("flow test 2");

        flow.addNode(new TimerNode("timer", 500))
            .addNode(new SplitNode("split", "tick", 3))
            .addNode(new PrintNode("normal"))
            .addNode(new PrintNode("warn"));

        flow.connect("timer", "out", "split", "in")
            .connect("split", "match", "print", "in")
            .connect("split", "mismatch", "print", "in");

        flow.initialize();

//        Thread split = new Thread(() -> {
//            while(!Thread.currentThread().isInterrupted()) {
//                Message message = conn1.poll();
//                if(message == null) break;
//                splitNode.process(message);
//            }
//        });
//
//        Thread normalPrint = new Thread(() -> {
//            while (!Thread.currentThread().isInterrupted()) {
//                Message message = conn2.poll();
//                if(message == null) break;
//                normalPrintNode.process(message);
//            }
//        });
//
//        Thread warnPrint = new Thread(() -> {
//            while (!Thread.currentThread().isInterrupted()) {
//                Message message = conn3.poll();
//                if(message == null) break;
//                warnPrintNode.process(message);
//            }
//        });
//
//        split.start();
//        normalPrint.start();
//        warnPrint.start();
//
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        flow.shutdown();
//
//        split.interrupt();
//        normalPrint.interrupt();
//        warnPrint.interrupt();
//
//        try {
//            split.join();
//            normalPrint.join();
//            warnPrint.join();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
}
