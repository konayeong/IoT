package com.fbp.engine.node.mqtt;

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

// MQTT Broker에서 메시지를 수신하여 FBP 플로우에 주입하는 소스 노드
@Slf4j
public class MqttSubScriberNode extends ProtocolNode {

    private final MqttClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MqttSubScriberNode(String id, Map<String, Object> config) {
        super(id, config);

        try {
            String brokerUrl = (String) getConfig("brokerUrl");
            String clientId = (String) getConfig("clientId");

            this.client = new MqttClient(
                    brokerUrl,
                    clientId,
                    new MemoryPersistence()
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        addOutputPort("out");
    }

    @Override
    protected void connect() throws Exception {
        try {
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setAutomaticReconnect(true);
            options.setCleanStart(true);

            client.connect(options);

            String topic = (String) getConfig("topic");
            Object qosValue = getConfig("qos");
            int qos = (qosValue instanceof Number) ? ((Number) qosValue).intValue() : 1;


            // 메시지 도착 시 실행할 함수(콜백)를 바로 넘김
            // IMqttMessageListener - messageArrived(실제 수신된 topic, MQTT 메시지 객체)
            client.setCallback(new MqttCallback() {
                @Override
                public void disconnected(MqttDisconnectResponse disconnectResponse) {
                }

                @Override
                public void mqttErrorOccurred(MqttException exception) {
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
                }

                @Override
                public void authPacketArrived(int reasonCode, MqttProperties properties) {
                }
            });
            client.subscribe(topic, qos);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void disconnect() throws Exception {
        if(client != null && client.isConnected()) {
            client.disconnect();
            client.close();
        }
    }

    @Override
    protected void onProcess(Message message) {
    }

    // 파싱
    protected Map<String, Object> parsePayload(String payloadStr) {
        try {
            return objectMapper.readValue(
                    payloadStr,
                    new TypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("rawPayload", payloadStr);
            return fallback;
        }
    }
}
