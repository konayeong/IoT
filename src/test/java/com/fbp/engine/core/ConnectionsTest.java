package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LocalConnectionTest {
    private LocalConnection connection;
    private Message message;

    @BeforeEach
    void setUp() {
        connection = new LocalConnection("conn-1");
        message = new Message(Map.of("temperature", 25.0));
    }

    @Test
    @DisplayName("deliver-poll 기본 동작")
    void deliver_poll() throws InterruptedException {
        connection.deliver(message);
        Message pollMsg = connection.poll();

        assertEquals(pollMsg, message);
    }

    @Test
    @DisplayName("메시지 순서 보장")
    void message_fifo() throws InterruptedException {
        Message message2 = new Message(Map.of("test2", "value"));
        Message message3 = new Message(Map.of("test3", 0));

        connection.deliver(message);
        connection.deliver(message2);
        connection.deliver(message3);

        assertEquals(message, connection.poll());
        connection.poll(); // message2
        assertEquals(message3, connection.poll());
    }

    @Test
    @DisplayName("멀티스레드 deliver-poll")
    void multiThread_deliver_poll() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            Message msg = null;
            try {
                msg = connection.poll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            assertEquals(25.0, msg.get("temperature"));
            latch.countDown();
        }).start();

        new Thread(() -> {
            connection.deliver(message);
        }).start();

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("poll 대기 동작")
    void poll_wait() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        // 소비자 시작
        new Thread(() -> {
            Message msg = null; //  대기
            try {
                msg = connection.poll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            assertEquals(25.0, msg.get("temperature"));
            latch.countDown();
        }).start();

        Thread.sleep(500);

        connection.deliver(message);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("버퍼 크기 제한")
    void bufferSize_boundary() throws InterruptedException {
        LocalConnection conn = new LocalConnection("conn-2", 2);
        CountDownLatch latch = new CountDownLatch(1);

        Thread producer = new Thread(() -> {
            try {
                conn.deliver(new Message(Map.of("test1", 1)));
                conn.deliver(new Message(Map.of("test2", 2)));
                conn.deliver(new Message(Map.of("test3", 3))); // 블로킹

                latch.countDown(); // 실행되면 안됨
            } catch (Exception e) {
                // ignore
            }
        });

        producer.start();

        // 아직 latch 안 내려갔어야 정상
        assertEquals(1, latch.getCount());
        conn.poll(); // 공간 확보

        // 이제 unblock됨
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("버퍼 크기 조회")
    void bufferSize() {
        connection.deliver(message);
        assertEquals(1, connection.getBufferSize());
    }
}