package com.fbp.engine.node;

import com.fbp.engine.core.ProtocolNode;
import com.fbp.engine.message.Message;
import com.fbp.engine.protocol.ModbusException;
import com.fbp.engine.protocol.ModbusTcpClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * MODBUS 장비에서 레지스터 값을 읽어 FBP 플로우에 주입하는 노드
 * "trigger" 포트로 메시지를 받을 때마다 레지스터를 읽음
 * config
 * host(S), port(i, 502), slaveId(i), startAddress(i), count(i), registerMapping(Map<S,O>, 선택)
 */
public class ModbusReaderNode extends ProtocolNode {

    private ModbusTcpClient client;

    public ModbusReaderNode(String id, Map<String, Object> config) {
        super(id, config);
        addInputPort("trigger");
        addOutputPort("out");
        addOutputPort("error");
    }

    @Override
    protected void connect() throws Exception {
        String host = (String) getConfig("host");
        int port = getConfig("port") == null ? 502 : (int) getConfig("port");
        client = new ModbusTcpClient(host, port);

        client.connect();
    }

    @Override
    protected void disconnect() throws Exception {
        client.disconnect();
    }

    @Override
    protected void onProcess(Message message) {
        int slaveId = (int) getConfig("slaveId");
        int startAddress = (int) getConfig("startAddress");
        int count = (int) getConfig("count");

        Map<String, Object> mapping = (Map<String, Object>) getConfig("registerMapping");

        try {
            int[] result = client.readHoldingRegisters(slaveId, startAddress, count);

            Map<String, Object> payload = new HashMap<>();

            if (mapping != null) { // registerMapping이 있으면
                for (Map.Entry<String, Object> entry : mapping.entrySet()) {
                    String key = entry.getKey();
                    int index = (int) entry.getValue();

                    if (index >= 0 && index < result.length) {
                        payload.put(key, result[index]);
                    }
                }
            } else {
                for (int i = 0; i < result.length; i++) {
                    payload.put("r" + (startAddress + i), result[i]);
                }
            }

            Message outMsg = new Message(payload);

            send("out", outMsg);

        } catch (IOException | ModbusException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            send("error", new Message(err));
        }

    }
}
