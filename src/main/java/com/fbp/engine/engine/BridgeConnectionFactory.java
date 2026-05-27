package com.fbp.engine.engine;

import com.fbp.engine.core.conn.Connection;
import com.fbp.engine.core.conn.LocalConnection;
import com.fbp.engine.core.conn.MqttBridgeConnection;
import com.fbp.engine.core.serializer.JsonMessageSerializer;
import com.fbp.engine.parser.definition.ConnectionDefinition;
import com.fbp.engine.parser.definition.FlowDefinition;
import com.fbp.engine.parser.definition.TransportDefinition;
import com.fbp.engine.parser.TransportType;

/**
 * 설정을 기반으로 Local 또는 MQTT 브릿지 커넥션을 동적으로 생성
 * transport 설정이 없거나 type이 local이면 LocalConnection
 * type이 mqtt이면 MqttBridgeConnection 리턴
 */
public class BridgeConnectionFactory implements ConnectionFactory {

    @Override
    public Connection create(FlowDefinition flow, ConnectionDefinition conn) {
        // 내부 디버깅 및 관리를 위한 커넥션 고유 고유 ID
        String connId = String.format("w-%s-%s->%s", flow.getId(), conn.fromNode(), conn.toNode());
        TransportDefinition transport = flow.getTransport();

        // 1. transport 없거나 LOCAL이면 기존 로컬 커넥션 반환
        if (transport == null || transport.type() == TransportType.LOCAL) {
            return new LocalConnection(connId);
        }

        // 2. MQTT 분산 환경인 경우
        if (transport.type() == TransportType.MQTT) {
            // 과제 요구사항 스펙 준수: fbp/{flow-id}/{sourceNode}.{sourcePort}→{targetNode}.{targetPort}
            String mqttTopic = String.format("fbp/%s/%s.%s→%s.%s",
                    flow.getId(),
                    conn.fromNode(), conn.fromPort(),
                    conn.toNode(), conn.toPort()
            );

            // 연결선 전용 ID, 브로커URI, 변환된 토픽, 직렬화기, QoS 주입
            MqttBridgeConnection mqtt = new MqttBridgeConnection(
                    connId,
                    transport.brokerUri(),
                    mqttTopic,
                    new JsonMessageSerializer(),
                    transport.qos()
            );

            // 공유 매니저 연결 및 라우팅 등록 실행
            mqtt.connect();
            return mqtt;
        }

        throw new IllegalStateException("지원하지 않는 transport 타입입니다: " + transport.type());
    }
}