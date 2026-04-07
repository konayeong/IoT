package com.fbp.engine.demo.step4;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Step4_2B {

    static BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        System.out.println("==== Step4-2-B ====");

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    queue.put("메시지-" + i);
                    Thread.sleep(100);
                }

                queue.put("END");
                System.out.println("producer stop");

            } catch (InterruptedException e) {}
        });

        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    String msg = queue.take();

                    if (msg.equals("END")) {
                        break;
                    }

                    System.out.println("consumer: " + msg);
                }
            } catch (InterruptedException e) {}
        });

        producer.start();
        consumer.start();
    }
}