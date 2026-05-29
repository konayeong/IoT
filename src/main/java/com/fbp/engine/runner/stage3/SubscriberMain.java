package com.fbp.engine.runner.stage3;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.core.JsonMessageSerializer;
import com.fbp.engine.core.MqttBridgeConnection;
import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.node.PrintNode;

public class SubscriberMain {

    public static void main(String[] args) {

        String broker = "tcp://localhost:1883";

        // 엔진
        FlowEngine engine = new FlowEngine(new MetricsCollector());

        // 플로우
        Flow flow = new Flow("subscriber-flow");

        // 노드
        PrintNode printer = new PrintNode("printer");

        flow.addNode(printer);

        // MQTT Connection
        MqttBridgeConnection mqttConn =
                new MqttBridgeConnection(
                        "mqtt-sub",
                        broker,
                        "fbp/test",
                        new JsonMessageSerializer(),
                        1
                );

        // 도착 포트 연결
        mqttConn.setTarget(printer.getInputPort("in"));

        mqttConn.connect();

        flow.getConnections().add(mqttConn);

        // 엔진 등록
        engine.register(flow);

        // 시작
        engine.startFlow("subscriber-flow");
    }
}