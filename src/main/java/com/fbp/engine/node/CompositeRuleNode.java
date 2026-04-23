package com.fbp.engine.node;

import com.fbp.engine.core.rule.RuleExpression;
import com.fbp.engine.message.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CompositeRuleNode extends AbstractNode{

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
        // TODO 다시 확인 필요
        conditions.add(expression::evaluate);
    }

    @Override
    protected void onProcess(Message message) {
        boolean result;

        if(operator.equals(Operator.AND)) {
           result = conditions.stream().allMatch(con -> con.test(message));
        }else {
            result = conditions.stream().anyMatch(con -> con.test(message));
        }

        if(result) {
            send("match", message);
        } else {
            send("mismatch", message);
        }
    }
}
