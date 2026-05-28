package com.fbp.engine.node;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class RuleNodeFlowTest {

    @Test
    void condition_match_should_go_to_match_port() throws InterruptedException {

        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow");

        RuleNode rule = new RuleNode("rule",
                msg -> (int) msg.getPayload().get("value") > 10);

        CollectorNode match = new CollectorNode("match");
        CollectorNode mismatch = new CollectorNode("mismatch");

        flow.addNode(rule)
                .addNode(match)
                .addNode(mismatch)
                .connect("rule", "match", "match", "in")
                .connect("rule", "mismatch", "mismatch", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        rule.process(new Message(Map.of("value", 20)));

        Thread.sleep(100);

        assertEquals(1, match.getCollects().size());
        assertEquals(0, mismatch.getCollects().size());

        engine.shutdown();
    }

    @Test
    void condition_mismatch_should_go_to_mismatch_port() throws InterruptedException {

        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow");

        RuleNode rule = new RuleNode("rule",
                msg -> (int) msg.getPayload().get("value") > 10);

        CollectorNode match = new CollectorNode("match");
        CollectorNode mismatch = new CollectorNode("mismatch");

        flow.addNode(rule)
                .addNode(match)
                .addNode(mismatch)
                .connect("rule", "match", "match", "in")
                .connect("rule", "mismatch", "mismatch", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        rule.process(new Message(Map.of("value", 5)));

        Thread.sleep(100);

        assertEquals(0, match.getCollects().size());
        assertEquals(1, mismatch.getCollects().size());

        engine.shutdown();
    }

    @Test
    void ports_should_exist() {

        RuleNode node = new RuleNode("rule", msg -> true);

        assertNotNull(node.getInputPort("in"));
        assertNotNull(node.getOutputPort("match"));
        assertNotNull(node.getOutputPort("mismatch"));
    }

    @Test
    void null_field_should_not_throw_exception() throws InterruptedException {

        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow");

        RuleNode rule = new RuleNode("rule",
                msg -> {
                    Object v = msg.getPayload().get("value");
                    return v != null && (int) v > 10;
                });

        CollectorNode match = new CollectorNode("match");
        CollectorNode mismatch = new CollectorNode("mismatch");

        flow.addNode(rule)
                .addNode(match)
                .addNode(mismatch)
                .connect("rule", "match", "match", "in")
                .connect("rule", "mismatch", "mismatch", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        assertDoesNotThrow(() ->
                rule.process(new Message(Map.of()))
        );

        Thread.sleep(100);

        assertEquals(0, match.getCollects().size());
        assertEquals(1, mismatch.getCollects().size());

        engine.shutdown();
    }

    @Test
    void multiple_messages_should_route_correctly() throws InterruptedException {

        FlowEngine engine = new FlowEngine();
        Flow flow = new Flow("flow");

        RuleNode rule = new RuleNode("rule",
                msg -> (int) msg.getPayload().get("value") > 10);

        CollectorNode match = new CollectorNode("match");
        CollectorNode mismatch = new CollectorNode("mismatch");

        flow.addNode(rule)
                .addNode(match)
                .addNode(mismatch)
                .connect("rule", "match", "match", "in")
                .connect("rule", "mismatch", "mismatch", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        rule.process(new Message(Map.of("value", 20)));
        rule.process(new Message(Map.of("value", 5)));
        rule.process(new Message(Map.of("value", 30)));

        Thread.sleep(200);

        assertEquals(2, match.getCollects().size());
        assertEquals(1, mismatch.getCollects().size());

        engine.shutdown();
    }
}