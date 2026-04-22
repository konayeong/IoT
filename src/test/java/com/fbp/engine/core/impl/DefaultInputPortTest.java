package com.fbp.engine.core.impl;

import com.fbp.engine.core.Node;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.in.PrintNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultInputPortTest {

    @Test
    @DisplayName("receive 시 owner 호출")
    void receive_call_owner() {
        // receive()하면 소속 노드의 process()가 호출됨
        Node node = mock(Node.class);
        DefaultInputPort defaultInputPort = new DefaultInputPort("in", node);
        Message message = new Message(Map.of());
        defaultInputPort.receive(message);

        verify(node, times(1)).process(any());
    }

    @Test
    @DisplayName("포트 이름 확인")
    void getName() {
        DefaultInputPort defaultInputPort = new DefaultInputPort("in", new PrintNode("print-1"));
        assertEquals("in", defaultInputPort.getName());
    }
}