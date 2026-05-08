//package com.fbp.engine.stage2.runner.step3;
//
//import com.fbp.engine.core.Flow;
//import com.fbp.engine.core.FlowEngine;
//import com.fbp.engine.message.Message;
//import com.fbp.engine.node.modbus.ModbusReaderNode;
//import com.fbp.engine.node.modbus.ModbusWriterNode;
//import com.fbp.engine.node.out.TimerNode;
//import com.fbp.engine.node.utils.ThresholdFilterNode;
//import com.fbp.engine.node.utils.TransformNode;
//import com.fbp.engine.protocol.ModbusTcpSimulator;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//public class Step3_9 {
//    public static void main(String[] args) throws IOException, InterruptedException {
//        ModbusTcpSimulator simulator = new ModbusTcpSimulator(5020, 10);
//
//        int[] initial = {350, 600, 0, 0, 0};
//        for (int i = 0; i < initial.length; i++) {
//            simulator.setRegister(i, initial[i]);
//        }
//
//        simulator.start();
//
//        Thread.sleep(500);
//
//        Map<String, Object> readerConfig = new HashMap<>();
//        readerConfig.put("host", "localhost");
//        readerConfig.put("port", 5020);
//        readerConfig.put("slaveId", 1);
//        readerConfig.put("startAddress", 0);
//        readerConfig.put("count", 1); // 온도
//        readerConfig.put("registerMapping", Map.of("temperature", 0));
//
//        // === 노드 생성 ===
//        TimerNode timer = new TimerNode("timer", 2000);
//        ModbusReaderNode readerNode = new ModbusReaderNode("read", readerConfig);
//        TransformNode transformNode = new TransformNode("transform", msg -> {
//            Map<String, Object> payload = new HashMap<>(msg.getPayload());
//
//            Number raw = (Number) payload.get("temperature");
//
//            if (raw != null) {
//                double scaled = raw.doubleValue() * 0.1; // 스케일 변환
//                payload.put("temperature", scaled);
//
//                payload.put("value", 1);
//            }
//
//            return new Message(payload);
//        });
//        ThresholdFilterNode filterNode = new ThresholdFilterNode("filter", "temperature", 30.5);
//
//        Map<String, Object> writerConfig = new HashMap<>();
//        writerConfig.put("host", "localhost");
//        writerConfig.put("port", 5020);
//        writerConfig.put("slaveId", 1);
//        writerConfig.put("registerAddress", 2);
//        writerConfig.put("valueField", "value"); // message 안에 value라는 키에서 값을 꺼내서 사용
//        writerConfig.put("scale", 1.0);
//
//        ModbusWriterNode writerNode = new ModbusWriterNode("write", writerConfig);
//
//        // === Flow ===
//        FlowEngine engine = new FlowEngine();
//        Flow flow = new Flow("flow");
//
//        flow.addNode(readerNode)
//            .addNode(transformNode)
//            .addNode(filterNode)
//            .addNode(writerNode)
//            .addNode(timer);
//
//        flow.connect("timer", "out", "read", "trigger")
//            .connect("read", "out", "transform", "in")
//            .connect("transform", "out", "filter", "in")
//            .connect("filter", "alert", "write", "in");
//
//        engine.register(flow);
//        engine.startFlow("flow");
//
//        Thread.sleep(100);
//        System.out.println("Register[2] : " + simulator.getRegister(2));
//
//        engine.shutdown();
//    }
//}
