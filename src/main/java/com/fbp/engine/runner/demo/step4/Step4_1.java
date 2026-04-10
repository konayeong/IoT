package com.fbp.engine.runner.demo.step4;

import java.util.ArrayList;
import java.util.List;

public class Step4_1 {
    private static List<String> buffer = new ArrayList<>();

    public static void run() {
        Thread producer = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                String msg = "메시지-" + i;
                buffer.add(msg);
                System.out.println("[producer] " + msg);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("producer stop");
        });

        Thread consumer = new Thread(() -> {
            while (true) {
                // busy-waiting 문제 발생
                // producer 종료, consumer는 계속 돌지만 아무 일 안 함
                if (!buffer.isEmpty()) {
                    String msg = buffer.removeFirst();
                    System.out.println("consumer: " + msg);
                }
            }
        });

        producer.start();
        consumer.start();
    }
}