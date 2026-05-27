package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class RuleExpressionTest {

    @Test
    void parse_numeric_expression_should_evaluate_correctly() {

        RuleExpression expr =
                RuleExpression.parse("temperature > 30.0");

        Message msg1 = new Message(Map.of("temperature", 35.0));
        Message msg2 = new Message(Map.of("temperature", 25.0));

        assertTrue(expr.evaluate(msg1));
        assertFalse(expr.evaluate(msg2));
    }

    @Test
    void parse_string_expression_should_evaluate_correctly() {

        RuleExpression expr =
                RuleExpression.parse("status == ON");

        Message on = new Message(Map.of("status", "ON"));
        Message off = new Message(Map.of("status", "OFF"));

        assertTrue(expr.evaluate(on));
        assertFalse(expr.evaluate(off));
    }

    @Test
    void all_operators_should_work_correctly() {

        Message msg = new Message(Map.of("value", 10));

        assertTrue(RuleExpression.parse("value > 5").evaluate(msg));
        assertTrue(RuleExpression.parse("value >= 10").evaluate(msg));
        assertTrue(RuleExpression.parse("value < 20").evaluate(msg));
        assertTrue(RuleExpression.parse("value <= 10").evaluate(msg));
        assertTrue(RuleExpression.parse("value == 10").evaluate(msg));
        assertTrue(RuleExpression.parse("value != 5").evaluate(msg));

        assertFalse(RuleExpression.parse("value > 20").evaluate(msg));
        assertFalse(RuleExpression.parse("value < 5").evaluate(msg));
        assertFalse(RuleExpression.parse("value == 5").evaluate(msg));
    }

    @Test
    void invalid_expression_should_throw_exception() {

        assertThrows(
                IllegalArgumentException.class,
                () -> RuleExpression.parse("temperature")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> RuleExpression.parse("temperature >")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> RuleExpression.parse("")
        );
    }

    @Test
    void missing_field_should_return_false() {

        RuleExpression expr =
                RuleExpression.parse("temperature > 30");

        Message msg = new Message(Map.of());

        assertFalse(expr.evaluate(msg));
    }
}