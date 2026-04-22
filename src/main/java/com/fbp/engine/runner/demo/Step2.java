package com.fbp.engine.runner.demo;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.in.PrintNode;

import java.util.HashMap;
import java.util.Map;

public class Step2 {
    public static void run() {
        System.out.println("==== Step2 ====");
        Map<String, Object> payload = new HashMap<>();
        payload.put("temperature", 25.5);

        Message message = new Message(payload);

        PrintNode printNode = new PrintNode("print-1");
        printNode.process(message);

        // 불변 확인
        Message newMessage = message.withEntry("add", 10);
        printNode.process(newMessage);

        printNode.process(message); // 원본 확인
    }
}
