package com.fbp.engine.node;

import com.fbp.engine.core.InputPort;
import com.fbp.engine.core.Node;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.core.impl.DefaultInputPort;
import com.fbp.engine.core.impl.DefaultOutputPort;
import com.fbp.engine.message.Message;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractNode implements Node {
    private String id;
    private Map<String, InputPort> inputPorts = new HashMap<>();
    private Map<String, OutputPort> outputPorts = new HashMap<>();

    protected AbstractNode(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void process(Message message) {
        System.out.println("[" + id + "] processing message...");

        onProcess(message);

        System.out.println("[" + id + "] processing complete.");
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

    // Default 포트 생성하여 맵에 등록
    protected void addInputPort(String name) {
        inputPorts.put(name, new DefaultInputPort(name, this));
    }

    protected void addOutputPort(String name) {
        outputPorts.put(name, new DefaultOutputPort(name));
    }

    // 포트 조회
    public InputPort getInputPort(String name) {
        return inputPorts.get(name);
    }

    public OutputPort getOutputPort(String name) {
        return outputPorts.get(name);
    }

    // 출력 포트로 메시지 전송
    protected void send(String portName, Message message) {
        OutputPort outputPort = outputPorts.get(portName);
        if(outputPort == null) {
            throw new IllegalArgumentException("OutputPort not found: " + portName);
        }
        outputPort.send(message);
    }

    protected abstract void  onProcess(Message message);
}
