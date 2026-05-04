package com.fbp.engine.node;

import com.fbp.engine.message.Message;
import lombok.Getter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// 테스트 - 노드 출력 검증
public class CollectorNode extends AbstractNode{
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    @Getter
    private final List<Message> collected = new ArrayList<>();

    public CollectorNode(String id) {
        super(id);
        addInputPort("in");
        addOutputPort("out"); // appTest
    }

    @Override
    public void onProcess(Message message) {
        collected.add(message);

        String timestamp = LocalTime.now().format(FORMATTER);
        System.out.println("[" + timestamp + "][" + getId() + "] " + message.getPayload());

        send("out", message);
    }


}
