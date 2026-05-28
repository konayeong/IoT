package com.fbp.engine.node;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositeRuleNodeTest {

    @Test
    void and_all_conditions_true_should_match() throws InterruptedException {

        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow");

        CompositeRuleNode node =
                new CompositeRuleNode("rule",
                        CompositeRuleNode.Operator.AND);

        node.addCondition("temperature", ">", 30);
        node.addCondition("humidity", "<", 60);

        CollectorNode match = new CollectorNode("match");
        CollectorNode mismatch = new CollectorNode("mismatch");

        flow.addNode(node)
                .addNode(match)
                .addNode(mismatch);

        flow.connect("rule", "match", "match", "in");
        flow.connect("rule", "mismatch", "mismatch", "in");

        engine.register(flow);
        engine.startFlow("flow");

        node.process(new Message(Map.of(
                "temperature", 35,
                "humidity", 40
        )));

        Thread.sleep(100);

        assertEquals(1, match.getCollects().size());
        assertEquals(0, mismatch.getCollects().size());

        engine.shutdown();
    }

    @Test
    void and_one_condition_false_should_mismatch() throws InterruptedException {

        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow");

        CompositeRuleNode node =
                new CompositeRuleNode("rule",
                        CompositeRuleNode.Operator.AND);

        node.addCondition("temperature", ">", 30);
        node.addCondition("humidity", "<", 60);

        CollectorNode match = new CollectorNode("match");
        CollectorNode mismatch = new CollectorNode("mismatch");

        flow.addNode(node)
                .addNode(match)
                .addNode(mismatch);

        flow.connect("rule", "match", "match", "in");
        flow.connect("rule", "mismatch", "mismatch", "in");

        engine.register(flow);
        engine.startFlow("flow");

        node.process(new Message(Map.of(
                "temperature", 35,
                "humidity", 80
        )));

        Thread.sleep(100);

        assertEquals(0, match.getCollects().size());
        assertEquals(1, mismatch.getCollects().size());

        engine.shutdown();
    }

    @Test
    void or_one_condition_true_should_match() throws InterruptedException {

        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow");

        CompositeRuleNode node =
                new CompositeRuleNode("rule",
                        CompositeRuleNode.Operator.OR);

        node.addCondition("temperature", ">", 30);
        node.addCondition("humidity", "<", 60);

        CollectorNode match = new CollectorNode("match");
        CollectorNode mismatch = new CollectorNode("mismatch");

        flow.addNode(node)
                .addNode(match)
                .addNode(mismatch);

        flow.connect("rule", "match", "match", "in");
        flow.connect("rule", "mismatch", "mismatch", "in");

        engine.register(flow);
        engine.startFlow("flow");

        node.process(new Message(Map.of(
                "temperature", 35,
                "humidity", 90
        )));

        Thread.sleep(100);

        assertEquals(1, match.getCollects().size());
        assertEquals(0, mismatch.getCollects().size());

        engine.shutdown();
    }

    @Test
    void or_all_conditions_false_should_mismatch() throws InterruptedException {

        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow");

        CompositeRuleNode node =
                new CompositeRuleNode("rule",
                        CompositeRuleNode.Operator.OR);

        node.addCondition("temperature", ">", 30);
        node.addCondition("humidity", "<", 60);

        CollectorNode match = new CollectorNode("match");
        CollectorNode mismatch = new CollectorNode("mismatch");

        flow.addNode(node)
                .addNode(match)
                .addNode(mismatch);

        flow.connect("rule", "match", "match", "in");
        flow.connect("rule", "mismatch", "mismatch", "in");

        engine.register(flow);
        engine.startFlow("flow");

        node.process(new Message(Map.of(
                "temperature", 10,
                "humidity", 90
        )));

        Thread.sleep(100);

        assertEquals(0, match.getCollects().size());
        assertEquals(1, mismatch.getCollects().size());

        engine.shutdown();
    }

    @Test
    void empty_conditions_should_follow_default_behavior() throws InterruptedException {

        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow");

        CompositeRuleNode andNode =
                new CompositeRuleNode("andRule",
                        CompositeRuleNode.Operator.AND);

        CompositeRuleNode orNode =
                new CompositeRuleNode("orRule",
                        CompositeRuleNode.Operator.OR);

        CollectorNode andMatch = new CollectorNode("andMatch");
        CollectorNode andMismatch = new CollectorNode("andMismatch");

        CollectorNode orMatch = new CollectorNode("orMatch");
        CollectorNode orMismatch = new CollectorNode("orMismatch");

        flow.addNode(andNode)
                .addNode(orNode)
                .addNode(andMatch)
                .addNode(andMismatch)
                .addNode(orMatch)
                .addNode(orMismatch);

        flow.connect("andRule", "match", "andMatch", "in");
        flow.connect("andRule", "mismatch", "andMismatch", "in");

        flow.connect("orRule", "match", "orMatch", "in");
        flow.connect("orRule", "mismatch", "orMismatch", "in");

        engine.register(flow);
        engine.startFlow("flow");

        Message msg = new Message(Map.of("value", 1));

        andNode.process(msg);
        orNode.process(msg);

        Thread.sleep(100);

        // AND + empty => true
        assertEquals(1, andMatch.getCollects().size());
        assertEquals(0, andMismatch.getCollects().size());

        // OR + empty => false
        assertEquals(0, orMatch.getCollects().size());
        assertEquals(1, orMismatch.getCollects().size());

        engine.shutdown();
    }
}