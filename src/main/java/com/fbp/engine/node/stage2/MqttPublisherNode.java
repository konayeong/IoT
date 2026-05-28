package com.fbp.engine.node.stage2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbp.engine.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class MqttPublisherNode extends ProtocolNode {

    private MqttClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private int errorCnt = 0;

    public MqttPublisherNode(String id, Map<String, Object> config) {
        super(id, config);
        addInputPort("in");
    }

    @Override
    protected void connect() throws Exception {

        String brokerUrl = (String) config.getOrDefault("brokerUrl", "tcp://localhost:1883");
        String clientId = (String) config.getOrDefault("clientId", "pub-" + getId());

        client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setAutomaticReconnect(true);
        options.setCleanStart(true);
        client.connect(options);

        log.info("[MQTT Publisher] Connected broker={}, clientId={}", client.getServerURI(), client.getClientId());
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
    protected void onProcess(Message message) {
        try {
            if (client == null || !client.isConnected()) {
                throw new IllegalStateException("MQTT client not connected");
            }

            String json = objectMapper.writeValueAsString(message.getPayload());

            Object topicValue = message.hasKey("topic") ? message.getPayload().get("topic") : getConfig("topic");

            if (topicValue == null) {
                throw new IllegalArgumentException("MQTT topic is required");
            }

            String topic = topicValue.toString();

            int qos = getConfig("qos") == null ? 1 : ((Number)getConfig("qos")).intValue();

            boolean retained = Boolean.TRUE.equals(getConfig("retained"));

            MqttMessage mqttMessage = new MqttMessage(json.getBytes(StandardCharsets.UTF_8));

            mqttMessage.setQos(qos);
            mqttMessage.setRetained(retained);

            client.publish(topic, mqttMessage);

        } catch (Exception e) {
            errorCnt++;
            log.error("[MQTT Publish Error] count={}, cause={}", errorCnt, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}