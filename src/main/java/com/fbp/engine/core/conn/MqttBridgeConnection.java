package com.fbp.engine.core.conn;

import com.fbp.engine.core.port.InputPort;
import com.fbp.engine.core.serializer.MessageSerializer;
import com.fbp.engine.message.Message;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MqttBridgeConnection implements Connection {

    @Getter
    private final String id;

    @Getter
    private final String brokerUri;

    @Getter
    private final String topic;

    @Getter
    private final int qos;

    private final MessageSerializer serializer;

    // MQTT 스레드가 수신한 메시지를 노드가 poll()해갈 때까지 안전하게 보관할 완충 우체통
    private final BlockingQueue<Message> internalQueue = new LinkedBlockingQueue<>();

    // 매니저로부터 공유받아 쓸 Publisher 클라이언트 참조
    private MqttClient publisher;

    @Getter @Setter
    private InputPort target; // 기존 포트 호환용 인터페이스 유지

    public MqttBridgeConnection(String id, String brokerUri, String topic, MessageSerializer serializer, int qos) {
        this.id = id;
        this.brokerUri = brokerUri;
        this.topic = topic;
        this.serializer = serializer;
        this.qos = qos;
        // 생성자 내에서 무겁게 client를 new하지 않고 선언만 유지합니다.
    }

    public void connect() {
        try {
            MqttClientManager manager = MqttClientManager.getInstance();

            // 1. 공용 매니저로부터 공유 Publisher 획득
            this.publisher = manager.getPubClient(brokerUri);

            // 2. 공용 매니저에 나(this)를 라우팅 테이블에 등록하고 공용 세션 구독 시작
            manager.registerRoute(topic, this);

        } catch (MqttException e) {
            throw new RuntimeException("MQTT Bridge 연결 및 라우팅 등록 실패", e);
        }
    }

    /**
     * MqttClientManager의 공용 네트워크 수신 스레드가 패킷을 받으면 호출하는 진입점
     */
    public void handleIncomingMqtt(byte[] payload) {
        try {
            Message msg = serializer.deserialize(payload);

            // 중요: MQTT 수신 스레드를 블로킹하지 않기 위해 internalQueue에 즉시 적재(완충)
            internalQueue.put(msg);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[" + id + "] 메시지 큐 적재 인터럽트 발생");
        } catch (Exception e) {
            System.err.println("[" + id + "] 메시지 디시리얼라이즈 실패: " + e.getMessage());
        }
    }

    @Override
    public void deliver(Message message) {
        try {
            if (publisher != null && publisher.isConnected()) {
                byte[] payload = serializer.serialize(message);
                publisher.publish(topic, payload, qos, false);
            } else {
                throw new RuntimeException("시스템 브로커 Publisher 커넥션이 열려있지 않습니다.");
            }
        } catch (MqttException e) {
            throw new RuntimeException("MQTT publish 실패: " + topic, e);
        }
    }

    @Override
    public Message poll() throws InterruptedException {
        // 이제 노드 러너 스레드는 자기가 MQTT 환경인지 모른 채,
        // internalQueue에 편지가 올 때까지 여기서 정상적으로 안전하게 대기(Blocking)합니다.
        return internalQueue.take();
    }

    @Override
    public int getBufferSize() {
        return internalQueue.size();
    }

    @Override
    public void close() {
        // 공용 소켓을 닫아버리면 다른 와이어들이 전부 끊기므로,
        // 전체 소켓을 close하지 않고 매니저의 내 라우팅 허가 주소만 안전하게 뺍니다.
        MqttClientManager.getInstance().unregisterRoute(topic);
        internalQueue.clear();
    }
}