package com.fbp.engine.stage2.runner;

import com.fbp.engine.core.EchoProtocolNode;

import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "localhost");
        config.put("port", 8888);

        EchoProtocolNode node = new EchoProtocolNode("node1", config);

        node.initialize();

        node.sendTestMessage("Hello FBP");

        node.shutdown();
    }
}