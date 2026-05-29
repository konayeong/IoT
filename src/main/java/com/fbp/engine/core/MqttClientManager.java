package com.fbp.engine.core;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// MQTT 소켓 연결 관리자
@Slf4j
public class MqttClientManager {

    private static final MqttClientManager INSTANCE = new MqttClientManager();

    private final Map<String, MqttClient> pubClients = new ConcurrentHashMap<>();
    private final Map<String, MqttClient> subClients = new ConcurrentHashMap<>();

    /**
     * topic -> 여러 bridge connection
     */
    private final Map<String, Set<MqttBridgeConnection>> router = new ConcurrentHashMap<>();

    private MqttClientManager() {}

    public static MqttClientManager getInstance() {
        return INSTANCE;
    }

    public synchronized MqttClient getPubClient(String brokerUri)
            throws MqttException {

        if (!pubClients.containsKey(brokerUri)) {

            MqttClient client =
                    new MqttClient(brokerUri, "fbp-pub-" + System.nanoTime());

            MqttConnectionOptions options = new MqttConnectionOptions();

            options.setAutomaticReconnect(true);
            options.setCleanStart(true);

            client.connect(options);

            pubClients.put(brokerUri, client);

            log.info("Publisher connected: {}", brokerUri);
        }

        return pubClients.get(brokerUri);
    }

    public synchronized MqttClient getSubClient(String brokerUri)
            throws MqttException {

        if (!subClients.containsKey(brokerUri)) {

            MqttClient client =
                    new MqttClient(brokerUri, "fbp-sub-" + System.nanoTime());

            MqttConnectionOptions options = new MqttConnectionOptions();

            options.setAutomaticReconnect(true);
            options.setCleanStart(true);

            client.setCallback(new MqttCallback() {

                @Override
                public void messageArrived(String topic, MqttMessage message) {

                    Set<MqttBridgeConnection> conns = router.get(topic);

                    if (conns == null) {
                        return;
                    }

                    for (MqttBridgeConnection conn : conns) {
                        conn.handleIncomingMqtt(message.getPayload());
                    }
                }

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if (reconnect) {
                        router.forEach((topic, conns) -> {

                            for (MqttBridgeConnection conn : conns) {
                                if (conn.getBrokerUri().equals(brokerUri)) {

                                    try {
                                        client.subscribe(topic, conn.getQos());
                                    } catch (MqttException e) {
                                        log.error("재구독 실패: {}", topic, e);
                                    }
                                }
                            }
                        });
                    }
                }

                @Override
                public void disconnected(MqttDisconnectResponse response) {}

                @Override
                public void mqttErrorOccurred(MqttException exception) {
                    log.error("MQTT error", exception);
                }

                @Override
                public void deliveryComplete(IMqttToken token) {}

                @Override
                public void authPacketArrived(int reasonCode, MqttProperties properties) {}
            });

            client.connect(options);

            subClients.put(brokerUri, client);

            log.info("Subscriber connected: {}", brokerUri);
        }

        return subClients.get(brokerUri);
    }

    public void registerRoute(String topic, MqttBridgeConnection conn) throws MqttException {
        router.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet()).add(conn);

        MqttClient subClient = getSubClient(conn.getBrokerUri());

        subClient.subscribe(topic, conn.getQos());

        log.info("Route registered: {} -> {}", topic, conn.getId());
    }

    /**
     * 라우팅 제거
     */
    public void unregisterRoute(String topic, MqttBridgeConnection conn) {
        Set<MqttBridgeConnection> conns = router.get(topic);

        if (conns == null) {
            return;
        }

        conns.remove(conn);

        if (conns.isEmpty()) {
            router.remove(topic);
        }

        log.info("Route removed: {} -> {}", topic, conn.getId());
    }
}