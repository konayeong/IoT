package com.fbp.engine.core;

import com.fbp.engine.parser.ConnectionDefinition;
import com.fbp.engine.parser.FlowDefinition;
import com.fbp.engine.parser.TransportDefinition;
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
        String connId = String.format("w-%s-%s->%s", flow.id(), conn.fromNode(), conn.toNode());
        TransportDefinition transport = flow.transport();

        // 1. transport 없거나 LOCAL이면 기존 로컬 커넥션 반환
        if (transport == null || transport.type() == TransportType.LOCAL) {
            return new LocalConnection(connId);
        }

        // 2. MQTT 분산 환경인 경우
        if (transport.type() == TransportType.MQTT) {

            String mqttTopic = String.format(
                    "fbp/%s/%s.%s-to-%s.%s",
                    flow.id(),
                    conn.fromNode(),
                    conn.fromPort(),
                    conn.toNode(),
                    conn.toPort()
            );

            int qos = transport.qos() == null ? 0 : transport.qos();

            return new MqttBridgeConnection(
                    connId,
                    transport.brokerUri(),
                    mqttTopic,
                    new JsonMessageSerializer(),
                    qos
            );
        }

        throw new IllegalStateException("지원하지 않는 transport 타입입니다: " + transport.type());
    }
}