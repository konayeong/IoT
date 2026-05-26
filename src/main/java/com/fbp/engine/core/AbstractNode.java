package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractNode implements Node {
    private final String id;

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
        System.out.println("[" + id + "] processing message");
        onProcess(message);
    }


    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

    protected void addInputPort(String name) {
        inputPorts.put(name, new DefaultInputPort(name, this));
    }

    protected void addOutputPort(String name) {
        outputPorts.put(name, new DefaultOutputPort(name));
    }

    public InputPort getInputPort(String name) {
        return inputPorts.get(name);
    }

    public OutputPort getOutputPort(String name) {
        return outputPorts.get(name);
    }

    protected void send(String portName, Message message) {
        OutputPort outputPort = outputPorts.get(portName);
        if(outputPort == null) {
            throw new IllegalArgumentException("OutputPort not found: " + portName);
        }
        outputPort.send(message);
    }

    protected abstract void onProcess(Message message);
}
