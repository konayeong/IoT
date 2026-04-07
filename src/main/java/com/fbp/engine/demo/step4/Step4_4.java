package com.fbp.engine.demo.step4;


import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.GeneratorNode;
import com.fbp.engine.node.PrintNode;

public class Step4_4 {

    public static void run() {
        System.out.println("==== Step4-4 ====");

        Connection conn = new Connection("conn-1");
        GeneratorNode generatorNode = new GeneratorNode("gen-1");
        PrintNode printNode = new PrintNode("print-1");

        generatorNode.getOutputPort().connect(conn);

        Thread consumer = new Thread(() -> {
            System.out.println("[consumer] start");

            while(true) {
                Message message = conn.poll(); // 대기
                System.out.println("[consumer] waiting");

                if("END".equals(message.get("type"))) {
                    break;
                }

                printNode.process(message);
                System.out.println("[consumer] " + message.getPayload());
            }

            System.out.println("[consumer] end");
        });

        Thread producer = new Thread(() -> {
            for(int i=0; i<5; i++) {
                try {
                    generatorNode.generate("key" + i, "value" + i);
                    System.out.println("[producer] message" + i);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }

            generatorNode.generate("type", "END");
            System.out.println("[producer] end");
        });

        consumer.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        producer.start();
    }
}
