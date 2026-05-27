package com.fbp.engine.node;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbp.engine.core.ProtocolNode;
import com.fbp.engine.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import java.util.HashMap;
import java.util.Map;

/**
 *             MQTT Broker
 *                  ↓
 *         MQTT Callback Thread
 *                  ↓
 *               Queue
 *                  ↓
 *         MqttSubscriberNode.onProcess
 *                  ↓
 *               send()
 *                  ↓
 *               Flow
 */
@Slf4j
public class MqttSubscriberNode extends ProtocolNode {

    private MqttClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MqttSubscriberNode(String id, Map<String, Object> config) {
        super(id, config);
        addOutputPort("out");
    }

    @Override
    protected void connect() throws Exception {
        // MqttClient 생성
        String brokerUrl = (String) config.getOrDefault("brokerUrl", "tcp://localhost:1883");
        String clientId = (String) config.getOrDefault("clientId", getId());
        String topic = (String) config.getOrDefault("topic", "#");
        int qos = ((Number) config.getOrDefault("qos", 1)).intValue(); // 최소 1회 전달 보장

        client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

        client.setCallback(new MqttCallback() {
            @Override
            public void disconnected(MqttDisconnectResponse disconnectResponse) {
                log.warn("[{}] MQTT disconnected", getId());
            }

            @Override
            public void mqttErrorOccurred(MqttException exception) {
                log.error("[{}] MQTT error", getId(), exception);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payloadStr = new String(message.getPayload());
                Map<String, Object> payload = parsePayload(payloadStr);

                payload.put("topic", topic);
                payload.put("mqttTimestamp", System.currentTimeMillis());

                send("out", new Message(payload));
            }

            @Override
            public void deliveryComplete(IMqttToken token) {

            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                log.info("[{}] MQTT connected (reconnect={})", getId(), reconnect);
            }

            @Override
            public void authPacketArrived(int reasonCode, MqttProperties properties) {
            }
        });

        // 연결 옵션 설정
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setAutomaticReconnect(true);
        options.setCleanStart(true);

        client.connect(options);
        client.subscribe(topic, qos);

        log.info("[{}] MQTT 연결 완료: {}", getId(), brokerUrl);
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
            log.error("[{}] MQTT disconnect error: {}", getId(), e.getMessage());
        }
    }

    @Override
    protected void onProcess(Message message) {
    }

    /**
     * JSON → Map 변환 (fallback 포함)
     */
    protected Map<String, Object> parsePayload(String payloadStr) {
        try {
            return objectMapper.readValue(
                    payloadStr,
                    new TypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            // JSON 파싱 실패 시 원본 payload를 "rawPayload" 키에 문자열로 넣어 전송
            fallback.put("rawPayload", payloadStr);
            return fallback;
        }
    }
}
