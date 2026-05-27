package com.fbp.engine.node;

import com.fbp.engine.core.ProtocolNode;
import com.fbp.engine.message.Message;
import com.fbp.engine.protocol.ModbusException;
import com.fbp.engine.protocol.ModbusTcpClient;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * FBP 플로우에서 처리된 결과를 MODBUS 장비의 레지스터에 기록하는 노드
 * config : host(S), port(i, 502), slaveId(i), registerAddress(i),
 *          valueField(S - FBP Message에서 값을 읽을 키), scale(d, 1.0)
 */
@Slf4j
public class ModbusWriterNode extends ProtocolNode {

    private ModbusTcpClient client;

    public ModbusWriterNode(String id, Map<String, Object> config) {
        super(id, config);
        addInputPort("in");
        addOutputPort("result");
    }

    @Override
    protected void connect() throws Exception {
        String host = (String) getConfig("host");
        int port = getConfig("port") == null ? 5020 : (int) getConfig("port");
        client = new ModbusTcpClient(host, port);

        client.connect();
    }

    @Override
    protected void disconnect() throws Exception {
        client.disconnect();
    }

    @Override
    protected void onProcess(Message message) {
        String valueField = (String) getConfig("valueField");
        Object scaleObj = getConfig("scale");
        double scale = (scaleObj instanceof Number) ? ((Number) scaleObj).doubleValue() : 1.0;
        int slaveId = (int) getConfig("slaveId");
        int registerAddress = (int) getConfig("registerAddress");

        try {
            Object rawValue = message.get(valueField); // value

            if (rawValue == null) {
                log.warn("값 없음: {}", valueField);
                return;
            }

            Number numValue = (Number) rawValue;
            // scale을 곱하여 정수로 변환
            int result = (int) Math.round(numValue.doubleValue() * scale);

            client.writeSingleRegister(slaveId, registerAddress, result);

            Map<String, Object> payload = new HashMap<>();
            payload.put("address", registerAddress);
            payload.put("value", result);

            send("result", new Message(payload));
        } catch (IOException | ModbusException e) {
            log.error("[ModbusWrite] Error : {}", e.getMessage());
        }
    }
}
