package com.fbp.engine.node;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.metrics.MetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RuleNodeFlowTest {

    private FlowEngine engine;
    private Flow flow;

    private RuleNode rule;
    private CollectorNode match;
    private CollectorNode mismatch;

    @BeforeEach
    void setUp() {

        engine = new FlowEngine(new MetricsCollector());
        flow = new Flow("flow");

        rule = new RuleNode(
                "rule",
                msg -> (int) msg.getPayload().get("value") > 10
        );

        match = new CollectorNode("match");
        mismatch = new CollectorNode("mismatch");

        flow.addNode(rule)
                .addNode(match)
                .addNode(mismatch);

        flow.connect("rule", "match", "match", "in");
        flow.connect("rule", "mismatch", "mismatch", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());
    }

    @Test
    void condition_match_should_go_to_match_port() throws Exception {

        rule.getInputPort("in")
                .receive(new Message(Map.of("value", 20)));

        Thread.sleep(100);

        assertEquals(1, match.getCollects().size());
        assertEquals(0, mismatch.getCollects().size());
    }

    @Test
    void condition_mismatch_should_go_to_mismatch_port() throws Exception {

        rule.getInputPort("in")
                .receive(new Message(Map.of("value", 5)));

        Thread.sleep(100);

        assertEquals(0, match.getCollects().size());
        assertEquals(1, mismatch.getCollects().size());
    }

    @Test
    void ports_should_exist() {

        assertNotNull(rule.getInputPort("in"));
        assertNotNull(rule.getOutputPort("match"));
        assertNotNull(rule.getOutputPort("mismatch"));
    }

    @Test
    void multiple_messages_should_route_correctly() throws Exception {

        rule.getInputPort("in")
                .receive(new Message(Map.of("value", 20)));
        rule.getInputPort("in")
                .receive(new Message(Map.of("value", 5)));
        rule.getInputPort("in")
                .receive(new Message(Map.of("value", 30)));

        Thread.sleep(200);

        assertEquals(2, match.getCollects().size());
        assertEquals(1, mismatch.getCollects().size());
    }
}