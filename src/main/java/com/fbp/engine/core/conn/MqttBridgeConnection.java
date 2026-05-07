package com.fbp.engine.core.conn;

import com.fbp.engine.core.serializer.MessageSerializer;
import com.fbp.engine.message.Message;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// BlockingQueue 대신 MQTT를 사용해서 노드 간 메시지를 전달하는 Connection 구현체
public class MqttBridgeConnection implements Connection{
    private final MqttClient client; // pub, sub 하나로 공유
    private final BlockingQueue<Message> internalQueue = new LinkedBlockingQueue<>();
    private final String topic;
    private final MessageSerializer serializer;
    private final int qos = 1;

    public MqttBridgeConnection(MqttClient client, String topic, MessageSerializer serializer) {
        this.client = client;
        this.topic = topic;
        this.serializer = serializer;

        initSubscriber();
    }

    // subscribe + callback 설정
    private void initSubscriber() {
        try {
            client.setCallback(new MqttCallback() {
                @Override
                public void disconnected(MqttDisconnectResponse disconnectResponse) {
                }

                @Override
                public void mqttErrorOccurred(MqttException exception) {
                }

                @Override
                public void messageArrived(String t, MqttMessage message) throws Exception {
                    if (topic.equals(t)) {
                        Message msg = serializer.deserialize(message.getPayload());
                        internalQueue.offer(msg);
                    }
                }

                @Override
                public void deliveryComplete(IMqttToken token) {}

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {}

                @Override
                public void authPacketArrived(int reasonCode, MqttProperties properties) {}
            });

            client.subscribe(topic,qos).waitForCompletion();
        } catch (Exception e) {
            throw new RuntimeException("MQTT subscribe 실패", e);
        }
    }

    // MQTT publish
    @Override
    public void deliver(Message message) {
        try {
            byte[] payload = serializer.serialize(message);
            client.publish(topic,payload,qos,false);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    // MQTT subscribe - 내부 큐에서 꺼냄
    @Override
    public Message poll() throws InterruptedException {
        return internalQueue.take();
    }

    @Override
    public int getBufferSize() {
        return internalQueue.size();
    }
}
