package com.fbp.engine.runner.demo.step6;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.*;
import com.fbp.engine.core.Flow;
// Step 7-2 재구성
public class Step6_5 {
    public static void run() throws InterruptedException {
        Flow flow = new Flow("flow test 1");

        // 노드 등록
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

        // initialize
        flow.initialize();

        Connection timerLog = flow.getConnections().get(0);
        Connection logFilter = flow.getConnections().get(1);
        Connection filterPrint = flow.getConnections().get(2);

        Thread t1 = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = timerLog.poll();
                if(message == null) break;
                logNode.process(message);
            }
        });

        Thread t2 = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                Message message = logFilter.poll();
                if(message == null) break;
                filterNode.process(message);
            }
        });

        Thread t3 = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = filterPrint.poll();
                if (message == null) break;
                printNode.process(message);
            }
        });

        t1.start();
        t2.start();
        t3.start();

        Thread.sleep(7000);

        flow.shutdown();

        t1.interrupt();
        t2.interrupt();
        t3.interrupt();

        t1.join();
        t2.join();
        t3.join();
    }
}
