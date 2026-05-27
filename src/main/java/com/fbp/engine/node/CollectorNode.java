package com.fbp.engine.node;

import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.message.Message;
import lombok.Getter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// 테스트 - 노드 출력 검증
public class CollectorNode extends AbstractNode {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    @Getter
    private final List<Message> collects = new ArrayList<>();

    public CollectorNode(String id) {
        super(id);
        addInputPort("in");
    }

    @Override
    public void onProcess(Message message) {
        collects.add(message);

        String timestamp = LocalTime.now().format(FORMATTER);
        System.out.println("[" + timestamp + "][" + getId() + "] " + message.getPayload());
    }
}
