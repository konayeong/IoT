package com.fbp.engine.runner.demo.step6;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.FilterNode;
import com.fbp.engine.node.LogNode;
import com.fbp.engine.node.PrintNode;
import com.fbp.engine.node.TimerNode;

public class Step6_5 {
    public static void run() throws InterruptedException {
        TimerNode timerNode = new TimerNode("timer", 500);
        LogNode logNode = new LogNode("log");
        FilterNode filterNode = new FilterNode("filter", "tick", 3);
        PrintNode printNode = new PrintNode("print");

        Connection timerLog = new Connection("timerLog");
        Connection logFilter = new Connection("logFilter");
        Connection filterPrint = new Connection("filterPrint");

        // connect
        timerLog.setTarget(logNode.getInputPort("in"));
        logFilter.setTarget(filterNode.getInputPort("in"));
        filterPrint.setTarget(printNode.getInputPort("in"));

        timerNode.getOutputPort("out").connect(timerLog);
        logNode.getOutputPort("out").connect(logFilter);
        filterNode.getOutputPort("out").connect(filterPrint);

        // initialize
        timerNode.initialize();
        logNode.initialize();
        filterNode.initialize();
        printNode.initialize();

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

        timerNode.shutdown();
        logNode.shutdown();
        filterNode.shutdown();
        printNode.shutdown();

        t1.interrupt();
        t2.interrupt();
        t3.interrupt();

        t1.join();
        t2.join();
        t3.join();
    }
}
