package com.fbp.engine.stage2.tcp;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttMessage;

public class MqttTestClient {
    public static void main(String[] args) throws Exception {
        String broker = "tcp://localhost:1883";
        String clientId = "test-client-1";

        // 클라이언트 생성
        MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());

        // 옵션
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setAutomaticReconnect(true);
        options.setCleanStart(true);

        // 연결
        client.connect(options);
        System.out.println("MQTT Connected");

        // 구독
        client.subscribe("sensor/temperature", 1, (topic, msg) -> {
            String payload = new String(msg.getPayload());
            System.out.println("수신: " + topic + " → " + payload);
        });

        // 발행
        Thread.sleep(3000);

        MqttMessage message = new MqttMessage("25.5".getBytes());
        message.setQos(1);
        client.publish("sensor/temperature", message);
        System.out.println("[PUB] sent message");

        // 유지
        Thread.sleep(10000);
        client.disconnect();
        client.close();

    }
}
