package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * MQTT 기반 분산 Connection
 *
 * 역할:
 * - deliver() : MQTT broker로 publish
 * - handleIncomingMqtt() : MQTT 수신 메시지를 내부 큐에 적재
 * - poll() : 내부 큐에서 메시지를 꺼내 FlowEngine으로 전달
 */
@Slf4j
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

    /**
     * MQTT 네트워크 수신 메시지 임시 저장 버퍼
     */
    private final BlockingQueue<Message> internalQueue = new LinkedBlockingQueue<>();
    private MqttClient client;

    @Getter @Setter
    private InputPort target;

    public MqttBridgeConnection(String id, String brokerUri, String topic, MessageSerializer serializer, int qos) {
        this.id = id;
        this.brokerUri = brokerUri;
        this.topic = topic;
        this.serializer = serializer;
        this.qos = qos;
    }

    /**
     * MQTT 브로커 연결 및 라우팅 등록
     */
    public void connect() {
        try {
            MqttClientManager manager = MqttClientManager.getInstance();

            // 공용 client 획득
            this.client = manager.getPubClient(brokerUri);

            // topic 라우팅 등록
            manager.registerRoute(topic, this);

            log.info("[{}] MQTT bridge connected", id);

        } catch (MqttException e) {
            throw new RuntimeException("MQTT bridge connect 실패", e);
        }
    }

    /**
     * MQTT 네트워크 수신 스레드 진입점
     *
     * broker → callback → 여기로 들어옴
     */
    public void handleIncomingMqtt(byte[] payload) {

        try {
            Message message = serializer.deserialize(payload);

            // 네트워크 스레드 블로킹 방지용 완충 큐
            internalQueue.put(message);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            log.error("[{}] MQTT queue 적재 인터럽트", id);
        } catch (Exception e) {

            log.error("[{}] deserialize 실패", id, e);
        }
    }

    /**
     * Flow → MQTT publish
     */
    @Override
    public void deliver(Message message) {

        try {
            if (client == null || !client.isConnected()) {

                throw new IllegalStateException("MQTT client 연결 안됨");
            }

            byte[] payload = serializer.serialize(message);

            client.publish(topic, payload, qos, false);

        } catch (MqttException e) {
            log.error("[{}] MQTT publish 실패", id, e);

            throw new RuntimeException("MQTT publish 실패", e);
        }
    }

    @Override
    public Message poll() throws InterruptedException {
        return internalQueue.take();
    }

    @Override
    public int getBufferSize() {
        return internalQueue.size();
    }

    @Override
    public void close() {

        try {
            MqttClientManager.getInstance().unregisterRoute(topic, this);

            internalQueue.clear();

            log.info("[{}] MQTT bridge closed", id);

        } catch (Exception e) {

            log.error("[{}] close 실패", id, e);
        }
    }
}