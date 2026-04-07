package com.fbp.engine.demo.step4;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.FilterNode;
import com.fbp.engine.node.GeneratorNode;
import com.fbp.engine.node.PrintNode;

import java.util.Map;

public class Step4_5 {

    private static volatile boolean running = true;

    public static void run() {
        System.out.println("==== Step4-5 ====");

        Connection conn1 = new Connection("conn-1");
        Connection conn2 = new Connection("conn-2");

        GeneratorNode generator = new GeneratorNode("gen-1");
        FilterNode filter = new FilterNode("filter-1", "temp", 5);
        PrintNode print = new PrintNode("print-1");

        generator.getOutputPort().connect(conn1);
        filter.getOutputPort().connect(conn2);

        Thread producer = new Thread(() -> {
            for (int i = 0; i < 10 && running; i++) {
                try {
                    generator.generate("temp", i);
                    System.out.println("[GEN] " + i);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }

            conn1.deliver(new Message(Map.of("type", "END")));
        });

        Thread t = new Thread(() -> {
            while (running) {
                Message msg = conn1.poll();

                if ("END".equals(msg.get("type"))) {
                    conn2.deliver(msg);
                    break;
                }

                filter.process(msg);
            }
        });

        Thread consumer = new Thread(() -> {
            while (running) {
                Message msg = conn2.poll();

                if ("END".equals(msg.get("type"))) {
                    running = false;
                    break;
                }

                print.process(msg);
            }
        });

        producer.start();
        t.start();
        consumer.start();
    }
}