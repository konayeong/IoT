package com.fbp.engine.node.stage2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbp.engine.core.ConnectionState;
import com.fbp.engine.message.Message;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MqttSubscriberNode extends ProtocolNode {

    private MqttClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MqttSubscriberNode(String id, Map<String, Object> config) {
        super(id, config);
        addOutputPort("out");
    }

    @Override
    protected void connect() throws Exception {

        String brokerUrl = (String) config.getOrDefault("brokerUrl", "tcp://localhost:1883");
        String clientId = (String) config.getOrDefault("clientId", "sub-" + getId());
        String topic = (String) config.getOrDefault("topic", "sensor/#");
        int qos = ((Number) config.getOrDefault("qos", 1)).intValue();

        client = new MqttClient(brokerUrl, clientId, new MemoryPersistence()); // MqttClient 생성

        // 연결 옵션 설정
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setAutomaticReconnect(true);
        options.setCleanStart(true);

        client.connect(options);

        client.setCallback(new MqttCallback() {

            @Override
            public void disconnected(MqttDisconnectResponse response) {
                connectionState = ConnectionState.ERROR;
                reconnect();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {

                String payloadStr = new String(message.getPayload(), StandardCharsets.UTF_8);

                Map<String,Object> payload = parsePayload(payloadStr);

                payload.put("topic", topic);
                payload.put("mqttTimestamp", System.currentTimeMillis());

                send("out", new Message(payload));
            }

            @Override public void mqttErrorOccurred(MqttException e){}
            @Override public void deliveryComplete(IMqttToken token){}
            @Override public void connectComplete(boolean r,String s){}
            @Override public void authPacketArrived(int r, MqttProperties p){}
        });

        client.subscribe(topic, qos);
    }

    @Override
    protected void disconnect() throws Exception {
        if (client != null) {
            if(client.isConnected()) {
                client.disconnect();
            }
            client.close();
        }
    }

    @Override
    protected void onProcess(Message message) {}

    protected Map<String,Object> parsePayload(String payloadStr) {
        try {
            return objectMapper.readValue(payloadStr, new TypeReference<>() {}
            );
        } catch (Exception e) {
            Map<String,Object> fallback = new HashMap<>();
            fallback.put("rawPayload", payloadStr);
            return fallback;
        }
    }
}
