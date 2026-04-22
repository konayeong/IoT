package com.fbp.engine.node.in;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

public class AlertNode extends AbstractNode {

    public AlertNode(String id) {
        super(id);
        addInputPort("in");
    }

    @Override
    public void onProcess(Message message) {
        if(!message.hasKey("sensorId")) {
            System.out.println("[경고] 알 수 없는 센서 데이터");
        }
        String sensorId = message.get("sensorId");

        if(message.hasKey("temperature")) {
            double temperature = message.get("temperature");
            System.out.println("[경고] 센서 " + sensorId + " 온도 " + temperature + "°C — 임계값 초과!");
        }else if(message.hasKey("humidity")) {
            double humidity = message.get("humidity");
            System.out.println("[경고] 센서 " + sensorId + "습도 " + humidity + "% - 임계값 초과");
        }

    }
}
