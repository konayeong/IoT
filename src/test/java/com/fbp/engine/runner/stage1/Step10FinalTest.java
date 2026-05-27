package com.fbp.engine.runner.stage1;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.*;
import org.junit.jupiter.api.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Step10FinalTest {
    private FlowEngine engine;
    private Flow flow;
    private static final String PATH = "stage1-final-test.txt";

    @BeforeEach
    void setUp() {
        engine = new FlowEngine();
        flow = new Flow("stage1-final-test");

        flow.addNode(new TimerNode("timer", 1000))
                .addNode(new TemperatureSensorNode("sensor", 15, 45))
                .addNode(new ThresholdFilterNode("threshold", "temperature", 30))
                .addNode(new CollectorNode("alertNode"))
                .addNode(new CollectorNode("logCollector"))
                .addNode(new LogNode("log"))
                .addNode(new FileWriterNode("file", PATH));

        flow.connect("timer", "out", "sensor", "trigger")
                .connect("sensor", "out", "threshold", "in")
                .connect("threshold", "alert", "alertNode", "in")
                .connect("threshold", "normal", "log", "in")
                .connect("log", "out", "file", "in")
                .connect("log", "out", "logCollector", "in");

        engine.register(flow);
    }

    @Test
    @DisplayName("엔진 시작/종료")
    void engine_start_end() throws InterruptedException {
        engine.startFlow(flow.getId());

        Thread.sleep(5000);

        assertEquals(FlowEngine.State.RUNNING, engine.getState());
        assertEquals(Flow.FlowState.RUNNING, flow.getFlowState());

        engine.shutdown();
        assertEquals(FlowEngine.State.STOPPED, engine.getState());
        assertEquals(Flow.FlowState.STOPPED, flow.getFlowState());
    }

    @Nested
    @DisplayName("수집 메시지 검증")
    class message {

        @BeforeEach
        void setUp() throws InterruptedException {
            engine.startFlow(flow.getId());

            Thread.sleep(7000); // 7초 대기
        }

        @AfterEach
        void tearDown() {
            engine.shutdown();
            new File(PATH).delete();
        }

        @Test
        @DisplayName("alert 경로 정확성")
        void alert_message() {
            CollectorNode cn = (CollectorNode) flow.getNodes().get("alertNode");
            for(Message msg : cn.getCollected()) {
                assertTrue((Double) msg.getPayload().get("temperature") > 30);
            }
        }

        @Test
        @DisplayName("normal 경로 정확성")
        void normal_message() {
            CollectorNode cn = (CollectorNode) flow.getNodes().get("logCollector");
            for(Message msg : cn.getCollected()) {
                assertTrue((Double) msg.getPayload().get("temperature") <= 30);
            }
        }

        @Test
        @DisplayName("전체 분기 완전성")
        void count_message() {
            CollectorNode alert = (CollectorNode) flow.getNodes().get("alertNode");
            CollectorNode log = (CollectorNode) flow.getNodes().get("logCollector");

            int total = alert.getCollected().size() + log.getCollected().size();

            TimerNode tn = (TimerNode) flow.getNodes().get("timer");
            assertEquals(tn.getTickCount(), total);
        }

        @Test
        @DisplayName("파일 기록 검증")
        void file() throws IOException {
            List<String> lines = Files.readAllLines(Path.of(PATH));

            CollectorNode cn = (CollectorNode) flow.getNodes().get("logCollector");
            int collect = cn.getCollected().size();
            int file = lines.size();

            assertEquals(collect, file);
        }

        @Test
        @DisplayName("센서 데이터 형식")
        void sensor_data_format() {
            CollectorNode alert = (CollectorNode) flow.getNodes().get("alertNode");
            CollectorNode log = (CollectorNode) flow.getNodes().get("logCollector");

            for(Message msg : alert.getCollected()) {
                assertAll(
                        () -> assertTrue(msg.getPayload().containsKey("sensorId")),
                        () -> assertTrue(msg.getPayload().containsKey("temperature")),
                        () -> assertTrue(msg.getPayload().containsKey("unit"))
                );
            }

            for(Message msg : log.getCollected()) {
                assertAll(
                        () -> assertTrue(msg.getPayload().containsKey("sensorId")),
                        () -> assertTrue(msg.getPayload().containsKey("temperature")),
                        () -> assertTrue(msg.getPayload().containsKey("unit"))
                );
            }
        }

        @Test
        @DisplayName("온도 범위")
        void temperature_range() {
            CollectorNode alert = (CollectorNode) flow.getNodes().get("alertNode");
            CollectorNode log = (CollectorNode) flow.getNodes().get("logCollector");

            for(Message msg : alert.getCollected()) {
                assertTrue((double) msg.getPayload().get("temperature") >= 15.0);
                assertTrue((double) msg.getPayload().get("temperature") <= 45.0);
            }

            for(Message msg : log.getCollected()) {
                assertTrue((double) msg.getPayload().get("temperature") >= 15.0);
                assertTrue((double) msg.getPayload().get("temperature") <= 45.0);
            }
        }
    }
}