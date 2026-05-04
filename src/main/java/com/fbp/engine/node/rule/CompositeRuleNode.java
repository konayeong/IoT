package com.fbp.engine.node.rule;

import com.fbp.engine.core.rule.RuleExpression;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

// 복합 규칙
public class CompositeRuleNode extends AbstractNode {

    private enum Operator {
        AND,
        OR
    }

    private final List<Predicate<Message>> conditions = new ArrayList<>();
    private final Operator operator;

    public CompositeRuleNode(String id, Operator operator) {
        super(id);
        this.operator = operator;
        addInputPort("in");
        addOutputPort("match");
        addOutputPort("mismatch");
    }

    public void addCondition(Predicate<Message> condition) {
        conditions.add(condition);
    }

    public void addCondition(String field, String op, Object value) {
        RuleExpression expression = new RuleExpression(field, op, value);
        conditions.add(expression::evaluate);
    }

    @Override
    protected void onProcess(Message message) {
        boolean result = false;

        if(operator.equals(Operator.AND)) {
           result = conditions.stream().allMatch(con -> con.test(message));
        }

        if(operator.equals(Operator.OR)) {
            result = conditions.stream().anyMatch(con -> con.test(message));
        }

        if(result) {
            send("match", message);
        } else {
            send("mismatch", message);
        }
    }
}
