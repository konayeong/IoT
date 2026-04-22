package com.fbp.engine.stage2.tcp;

import com.fbp.engine.core.OutputPort;
import com.fbp.engine.message.Message;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@RequiredArgsConstructor
public class MessageListenerImpl implements MessageListener{

    private final OutputPort outputPort;

    @Override
    public void onMessage(String topic, byte[] payload) {
        String data = new String(payload);

        Message message = new Message(
                Map.of(
                        "topic", topic,
                        "data", data,
                        "timestamp", System.currentTimeMillis()
                )
        );

        outputPort.send(message);
    }

    @Override
    public void onConnectionLost(Throwable cause) {

    }
}
