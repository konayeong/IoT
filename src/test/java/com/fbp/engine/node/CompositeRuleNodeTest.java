package com.fbp.engine.node;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.metrics.MetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositeRuleNodeTest {

    private FlowEngine engine;
    private Flow flow;

    private CompositeRuleNode node;

    private CollectorNode match;
    private CollectorNode mismatch;

    @BeforeEach
    void setUp() {

        engine = new FlowEngine(new MetricsCollector());
        flow = new Flow("flow");

        node = new CompositeRuleNode(
                "rule",
                CompositeRuleNode.Operator.AND
        );

        match = new CollectorNode("match");
        mismatch = new CollectorNode("mismatch");
    }

    // 공통 flow 구성
    private void setupFlowWithNode() {

        flow = new Flow("flow");

        flow.addNode(node)
                .addNode(match)
                .addNode(mismatch);

        flow.connect("rule", "match", "match", "in");
        flow.connect("rule", "mismatch", "mismatch", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());
    }

    @Test
    void and_all_conditions_true_should_match() throws Exception {

        node = new CompositeRuleNode("rule", CompositeRuleNode.Operator.AND);
        node.addCondition("temperature", ">", 30);
        node.addCondition("humidity", "<", 60);

        setupFlowWithNode();

        node.getInputPort("in")
                .receive(new Message(Map.of(
                        "temperature", 35,
                        "humidity", 40
                )));

        Thread.sleep(100);

        assertEquals(1, match.getCollects().size());
        assertEquals(0, mismatch.getCollects().size());
    }

    @Test
    void and_one_condition_false_should_mismatch() throws Exception {

        node = new CompositeRuleNode("rule", CompositeRuleNode.Operator.AND);
        node.addCondition("temperature", ">", 30);
        node.addCondition("humidity", "<", 60);

        setupFlowWithNode();

        node.getInputPort("in")
                .receive(new Message(Map.of(
                        "temperature", 35,
                        "humidity", 80
                )));

        Thread.sleep(100);

        assertEquals(0, match.getCollects().size());
        assertEquals(1, mismatch.getCollects().size());
    }

    @Test
    void or_one_condition_true_should_match() throws Exception {

        node = new CompositeRuleNode("rule", CompositeRuleNode.Operator.OR);
        node.addCondition("temperature", ">", 30);
        node.addCondition("humidity", "<", 60);

        setupFlowWithNode();

        node.getInputPort("in")
                .receive(new Message(Map.of(
                        "temperature", 35,
                        "humidity", 90
                )));

        Thread.sleep(100);

        assertEquals(1, match.getCollects().size());
        assertEquals(0, mismatch.getCollects().size());
    }

    @Test
    void or_all_conditions_false_should_mismatch() throws Exception {

        node = new CompositeRuleNode("rule", CompositeRuleNode.Operator.OR);
        node.addCondition("temperature", ">", 30);
        node.addCondition("humidity", "<", 60);

        setupFlowWithNode();

        node.getInputPort("in")
                .receive(new Message(Map.of(
                        "temperature", 10,
                        "humidity", 90
                )));

        Thread.sleep(100);

        assertEquals(0, match.getCollects().size());
        assertEquals(1, mismatch.getCollects().size());
    }

    @Test
    void empty_conditions_should_follow_default_behavior() throws Exception {

        CompositeRuleNode andNode =
                new CompositeRuleNode("andRule", CompositeRuleNode.Operator.AND);

        CompositeRuleNode orNode =
                new CompositeRuleNode("orRule", CompositeRuleNode.Operator.OR);

        flow = new Flow("flow");

        flow.addNode(andNode)
                .addNode(orNode)
                .addNode(match)
                .addNode(mismatch);

        flow.connect("andRule", "match", "match", "in");
        flow.connect("andRule", "mismatch", "mismatch", "in");

        flow.connect("orRule", "match", "match", "in");
        flow.connect("orRule", "mismatch", "mismatch", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        Message msg = new Message(Map.of("value", 1));

        andNode.getInputPort("in").receive(msg);
        orNode.getInputPort("in").receive(msg);

        Thread.sleep(200);

        assertEquals(1, match.getCollects().size());
        assertEquals(1, mismatch.getCollects().size());
    }
}