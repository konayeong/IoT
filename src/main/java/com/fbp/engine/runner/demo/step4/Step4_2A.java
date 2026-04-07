package com.fbp.engine.runner.demo.step4;

import java.util.ArrayList;
import java.util.List;

public class Step4_2A {

    private static List<String> buffer = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("==== Step-4-2-A ====");

        // 데이터 넣으면 notify()
        Thread producer = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                synchronized (buffer) {
                    buffer.add("메시지-" + i);
                    buffer.notifyAll(); // 소비자 깨움
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }

            // 종료 시점 처리
            synchronized (buffer) {
                buffer.add("END");
                buffer.notifyAll();
            }

            System.out.println("producer stop");
        });

        // 데이터 없으면 wait()
        Thread consumer = new Thread(() -> {
            while (true) {
                synchronized (buffer) {
                    // 데이터 없으면 기다림
                    while (buffer.isEmpty()) {
                        try {
                            buffer.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }

                    String msg = buffer.removeFirst();

                    if (msg.equals("END")) {
                        break;
                    }
                }
            }
        });

        producer.start();
        consumer.start();
    }
}