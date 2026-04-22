package com.fbp.engine.node.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbp.engine.core.ProtocolNode;
import com.fbp.engine.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import java.io.IOException;
import java.util.Map;

// FBP Flow에서 처리된 결과를 MQTT Broker로 발행하는 싱크 노드
@Slf4j
public class MqttPublisherNode extends ProtocolNode {

    private final MqttClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private int errorCnt = 0;

    public MqttPublisherNode(String id, Map<String, Object> config) {
        super(id, config);

        try {
            String brokerUrl = (String) config.get("brokerUrl");
            String clientId = (String) config.get("clientId");

            this.client = new MqttClient(
                    brokerUrl,
                    clientId,
                    // MQTT Client가 사용하는 데이터 저장 방식
                    new MemoryPersistence()
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        addInputPort("in");
    }

    @Override
    protected void connect() throws Exception {
        try {
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setAutomaticReconnect(true);
            options.setCleanStart(true);

            client.connect(options);

            log.info("[MQTT Publisher] Connected / brokerUrl={}, clientId={}", client.getServerURI(), client.getClientId());

        } catch (Exception e) {
            log.error("[MQTT Publisher] Connection Failed");
            throw new IOException(e);
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
        try {
            String json = objectMapper.writeValueAsString(message.getPayload());

//            Object topicValue = message.hasKey("topic")
//                    ? message.getPayload().get("topic")
//                    : getConfig("topic");
            // TODO-Q 명세에서는 메시지에 "topic" 키가 있으면 그 값 사용, 없으면 config의 기본 토픽 사용 이렇게 말했는데..
            Object topicValue = getConfig("topic");
            String topic = topicValue == null ? null : topicValue.toString();

            int qos = getConfig("qos") == null ? 1 : ((Number) getConfig("qos")).intValue();
            boolean retained = getConfig("retained") != null && (boolean) getConfig("retained");

            MqttMessage mqttMessage = new MqttMessage(json.getBytes());
            mqttMessage.setQos(qos);
            mqttMessage.setRetained(retained);

            client.publish(topic, mqttMessage);
        } catch (Exception e) {
            errorCnt++;

            log.error("[MQTT Publish Error]");
            log.debug("error count = {}", errorCnt);

            throw new RuntimeException();
        }
    }
}
