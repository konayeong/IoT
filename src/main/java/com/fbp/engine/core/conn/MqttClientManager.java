package com.fbp.engine.core.conn;

import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 외부 MQTT 브로커와 연결되는 두개의 대형 통로
// 보내는 통로 : pubClients
// 받는 통로 : subClients
public class MqttClientManager {
    private static final MqttClientManager INSTANCE = new MqttClientManager();

    // 브로커URI별 단 하나의 Pub / Sub 클라이언트를 캐싱
    private final Map<String, MqttClient> pubClients = new ConcurrentHashMap<>();
    private final Map<String, MqttClient> subClients = new ConcurrentHashMap<>();

    // 토픽 문자열을 key로 삼아, 수신된 메시지를 배달할 Connection 인스턴스 매핑
    private final Map<String, MqttBridgeConnection> router = new ConcurrentHashMap<>();

    private MqttClientManager() {}

    public static MqttClientManager getInstance() {
        return INSTANCE;
    }

    // 공용 Publisher 클라이언트 획득
    public synchronized MqttClient getPubClient(String brokerUri) throws MqttException {
        if (!pubClients.containsKey(brokerUri)) {
            MqttClient client = new MqttClient(brokerUri, "fbp-core-pub-" + System.nanoTime());
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setAutomaticReconnect(true);
            options.setCleanStart(true);

            client.connect(options);
            pubClients.put(brokerUri, client);
        }
        return pubClients.get(brokerUri);
    }

    // 공용 Subscriber 클라이언트 획득 및 통합 콜백 설정
    public synchronized MqttClient getSubClient(String brokerUri) throws MqttException {
        if (!subClients.containsKey(brokerUri)) {
            MqttClient client = new MqttClient(brokerUri, "fbp-core-sub-" + System.nanoTime());
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setAutomaticReconnect(true);
            options.setCleanStart(true);

            client.setCallback(new MqttCallback() {
                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    // 라우팅 테이블에서 토픽에 해당하는 커넥션을 찾아 메시지 위임
                    MqttBridgeConnection conn = router.get(topic);
                    if (conn != null) {
                        conn.handleIncomingMqtt(message.getPayload());
                    }
                }

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    // 브로커 일시 다운 후 재연결 시, 유실된 구독 정보(Subscription) 복구
                    if (reconnect) {
                        router.forEach((topic, conn) -> {
                            if (conn.getBrokerUri().equals(brokerUri)) {
                                try {
                                    client.subscribe(topic, conn.getQos());
                                } catch (MqttException e) {
                                    System.err.println("재연결 구독 복구 실패: " + topic);
                                }
                            }
                        });
                    }
                }

                @Override public void disconnected(MqttDisconnectResponse response) {}
                @Override public void mqttErrorOccurred(MqttException exception) { exception.printStackTrace(); }
                @Override public void deliveryComplete(IMqttToken token) {}
                @Override public void authPacketArrived(int reasonCode, MqttProperties properties) {}
            });

            client.connect(options);
            subClients.put(brokerUri, client);
        }
        return subClients.get(brokerUri);
    }

    // 특정 와이어가 통신을 시작할 때 매니저에 라우팅 등록 및 브로커 구독 요청
    public void registerRoute(String topic, MqttBridgeConnection conn) throws MqttException {
        router.put(topic, conn);
        MqttClient subClient = getSubClient(conn.getBrokerUri());
        subClient.subscribe(topic, conn.getQos());
    }

    // 와이어가 끊기거나 해제될 때 라우팅 제거
    public void unregisterRoute(String topic) {
        router.remove(topic);
    }
}