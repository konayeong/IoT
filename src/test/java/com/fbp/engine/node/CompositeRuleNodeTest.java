package com.fbp.engine.node;

import com.fbp.engine.core.Flow;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
class CompositeRuleNodeTest {

    @Test
    void and_all_conditions_true_should_match() {

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

        flow.initialize();

        node.process(new Message(Map.of(
                "temperature", 35,
                "humidity", 40
        )));

        assertEquals(1, match.getCollects().size());
        assertEquals(0, mismatch.getCollects().size());
    }

    @Test
    void and_one_condition_false_should_mismatch() {

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

        flow.initialize();

        node.process(new Message(Map.of(
                "temperature", 35,
                "humidity", 80
        )));

        assertEquals(0, match.getCollects().size());
        assertEquals(1, mismatch.getCollects().size());
    }

    @Test
    void or_one_condition_true_should_match() {

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

        flow.initialize();

        node.process(new Message(Map.of(
                "temperature", 35,
                "humidity", 90
        )));

        assertEquals(1, match.getCollects().size());
        assertEquals(0, mismatch.getCollects().size());
    }

    @Test
    void or_all_conditions_false_should_mismatch() {

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

        flow.initialize();

        node.process(new Message(Map.of(
                "temperature", 10,
                "humidity", 90
        )));

        assertEquals(0, match.getCollects().size());
        assertEquals(1, mismatch.getCollects().size());
    }

    @Test
    void empty_conditions_should_follow_default_behavior() {

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

        flow.initialize();

        Message msg = new Message(Map.of("value", 1));

        andNode.process(msg);
        orNode.process(msg);

        // AND + empty => true
        assertEquals(1, andMatch.getCollects().size());
        assertEquals(0, andMismatch.getCollects().size());

        // OR + empty => false
        assertEquals(0, orMatch.getCollects().size());
        assertEquals(1, orMismatch.getCollects().size());
    }
}