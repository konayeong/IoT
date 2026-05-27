package com.fbp.engine.node;

import com.fbp.engine.core.port.DefaultInputPort;
import com.fbp.engine.core.port.InputPort;
import com.fbp.engine.core.Node;
import com.fbp.engine.core.port.OutputPort;
import com.fbp.engine.core.port.DefaultOutputPort;
import com.fbp.engine.message.Message;
import com.fbp.engine.metrics.MetricsCollector;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractNode implements Node {
    private String id;
    private Map<String, InputPort> inputPorts = new HashMap<>();
    private Map<String, OutputPort> outputPorts = new HashMap<>();
    @Setter
    protected MetricsCollector metricsCollector;

    protected AbstractNode(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void process(Message message) {
        // Stage3
        long start = System.currentTimeMillis();
        log.debug("[{}] processing message..." , id);

        try {
            onProcess(message); // 핵심 로직 (하위 클래스에 위임)
            if (metricsCollector != null) {
                metricsCollector.recordProcessing(
                        id,
                        System.currentTimeMillis() - start,
                        true
                );
            }
            log.debug("[{}] processing complete..." , id);
        }catch (Exception e) {
            if(metricsCollector != null) {
                metricsCollector.recordProcessing(id, System.currentTimeMillis()- start, false);
            }
            throw e;
        }
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

    protected abstract void onProcess(Message message);
}
