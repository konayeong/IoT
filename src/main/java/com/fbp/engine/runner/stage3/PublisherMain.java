package com.fbp.engine.runner.stage3;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.core.JsonMessageSerializer;
import com.fbp.engine.core.MqttBridgeConnection;
import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.node.GeneratorNode;

public class PublisherMain {

    public static void main(String[] args) throws Exception {

        String broker = "tcp://localhost:1883";

        // 엔진
        FlowEngine engine = new FlowEngine(new MetricsCollector());

        // 플로우
        Flow flow = new Flow("publisher-flow");

        // 노드
        GeneratorNode generator = new GeneratorNode("generator");

        flow.addNode(generator);

        // MQTT Connection
        MqttBridgeConnection mqttConn =
                new MqttBridgeConnection(
                        "mqtt-pub",
                        broker,
                        "fbp/test",
                        new JsonMessageSerializer(),
                        1
                );

        mqttConn.connect();

        // Generator output → MQTT 연결
        generator.getOutputPort("out").connect(mqttConn);

        flow.getConnections().add(mqttConn);

        // 엔진 등록
        engine.register(flow);

        // 플로우 시작
        engine.startFlow("publisher-flow");

        // 메시지 전송 테스트
        int count = 0;

        while (true) {

            generator.generate("temperature", 20 + count++);

            Thread.sleep(1000);
        }
    }
}