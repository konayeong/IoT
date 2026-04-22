package com.fbp.engine.runner.demo;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.utils.FilterNode;
import com.fbp.engine.node.in.PrintNode;
import com.fbp.engine.node.out.TimerNode;

public class Step5 {
    public static void run() {
        TimerNode timerNode = new TimerNode("timer-1", 500);
        FilterNode filterNode = new FilterNode("filter-1", "tick", 3);
        PrintNode printNode = new PrintNode("print-1");

        Connection conn1 = new Connection("conn-1");
        Connection conn2 = new Connection("conn-2");

        conn1.setTarget(filterNode.getInputPort("in"));
        conn2.setTarget(printNode.getInputPort("in"));

        timerNode.getOutputPort("out").connect(conn1);
        filterNode.getOutputPort("out").connect(conn2);

        timerNode.initialize();
        filterNode.initialize();
        printNode.initialize();

        Thread filterThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Message msg = conn1.poll(); // TimerNode로부터 메시지 수신
                if(msg == null) {
                    break;
                }
                filterNode.process(msg);    // tick >= 3이면 conn2로 전달
            }
        }, "FilterThread");

        Thread printThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Message msg = conn2.poll(); // FilterNode로부터 메시지 수신
                if(msg == null) {
                    break;
                }
                printNode.process(msg);     // 출력
            }
        }, "PrintThread");

        filterThread.start();
        printThread.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        timerNode.shutdown();
        filterNode.shutdown();
        printNode.shutdown();

        filterThread.interrupt();
        printThread.interrupt();

        try {
            filterThread.join();
            printThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
