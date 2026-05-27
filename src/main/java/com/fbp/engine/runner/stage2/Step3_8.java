package com.fbp.engine.runner.stage2;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.ModbusReaderNode;
import com.fbp.engine.node.PrintNode;
import com.fbp.engine.node.TimerNode;
import com.fbp.engine.protocol.ModbusTcpSimulator;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Step3_8 {
    public static void main(String[] args) throws IOException, InterruptedException {
        ModbusTcpSimulator simulator = new ModbusTcpSimulator(5020, 10);

        int[] initial = {250, 600, 1, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < initial.length; i++) {
            simulator.setRegister(i, initial[i]);
        }

        simulator.start();
        log.debug("Simulator Start");

        Thread.sleep(5000);

        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow");

        TimerNode timerNode = new TimerNode("timer", 1000);

        Map<String, Object> readerConfig = new HashMap<>();
        readerConfig.put("host", "localhost");
        readerConfig.put("port", 5020);
        readerConfig.put("slaveId", 1);
        readerConfig.put("startAddress", 0);
        readerConfig.put("count", 3);

        ModbusReaderNode readerNode = new ModbusReaderNode("read", readerConfig);
        PrintNode printNode = new PrintNode("print");

        flow.addNode(timerNode)
            .addNode(readerNode)
            .addNode(printNode);

        flow.connect("timer", "out", "read", "trigger")
            .connect("read", "out", "print", "in" )
            .connect("read","error", "print","in");

        engine.register(flow);
        engine.startFlow("flow");

        Thread.sleep(10000);

        engine.shutdown();
    }
}
