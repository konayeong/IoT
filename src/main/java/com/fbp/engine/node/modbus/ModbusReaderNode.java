package com.fbp.engine.node.modbus;

import com.fbp.engine.core.ProtocolNode;
import com.fbp.engine.message.Message;
import com.fbp.engine.protocol.ModbusException;
import com.fbp.engine.protocol.ModbusTcpClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        int slaveId = (int) getConfig("slaveId");
        int startAddress = (int) getConfig("startAddress");
        int count = (int) getConfig("count");

        // TODO : 설계 가이드 참고해서 확장
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
