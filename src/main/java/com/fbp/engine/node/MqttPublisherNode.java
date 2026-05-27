package com.fbp.engine.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbp.engine.core.ProtocolNode;
import com.fbp.engine.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import java.util.Map;

@Slf4j
public class MqttPublisherNode extends ProtocolNode {

    private MqttClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private int errorCount = 0;

    public MqttPublisherNode(String id, Map<String, Object> config) {
        super(id, config);
        addInputPort("in");
    }

    @Override
    protected void connect() throws Exception {
        // MqttClient 생성
        String brokerUrl = (String) config.getOrDefault("brokerUrl", "tcp://localhost:1883");
        String clientId = (String) config.getOrDefault("clientId", getId());
        client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

        // 연결
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setAutomaticReconnect(true);
        options.setCleanStart(true);

        client.connect(options);

        // 연결 상태 로그 출력
        log.info("[{}] MQTT Publisher connected ({})", getId(), brokerUrl);

    }

    @Override
    protected void disconnect() throws Exception {
        try {
            if (client != null) {
                if (client.isConnected()) {
                    client.disconnect();
                }
                client.close();
            }
        } catch (Exception e) {
            log.error("[{}] disconnect error: {}", getId(), e.getMessage());
        }
    }

    @Override
    protected void onProcess(Message message) {
        if (!isConnected()) {
            log.warn("[{}] not connected → message dropped", getId());
            return;
        }

        try {
            // 1. payload → JSON
            String jsonPayload = objectMapper.writeValueAsString(message.getPayload());

            // 2. topic 결정
            String topic = message.getPayload().get("topic") == null
                    ? config.get("topic").toString() : message.getPayload().get("topic").toString();

            // 3. qos / retained 설정
            int qos = ((Number) config.getOrDefault("qos", 1)).intValue();
            boolean retained = (boolean) config.getOrDefault("retained", false);

            // 4. MQTT 메시지 생성
            MqttMessage mqttMessage = new MqttMessage(jsonPayload.getBytes());
            mqttMessage.setQos(qos);
            mqttMessage.setRetained(retained);

            // 5. publish
            client.publish(topic, mqttMessage);

            log.debug("[{}] published → topic={}, payload={}", getId(), topic, jsonPayload);

        } catch (Exception e) {
            errorCount++;
            // 발행 실패 시 로그 출력, 예외를 삼키지 않고 에러 카운트 관리
            log.error("[{}] publish error (count={}): {}", getId(), errorCount, e.getMessage());
        }
    }
}
