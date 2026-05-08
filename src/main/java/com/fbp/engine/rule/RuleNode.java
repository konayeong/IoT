package com.fbp.engine.rule;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;

import java.util.function.Predicate;

public class RuleNode extends AbstractNode {

    private final Predicate<Message> condition;

    // Predicate 기반
    public RuleNode(String id, Predicate<Message> condition) {
        super(id);
        this.condition = condition;
        addInputPort("in");
        addOutputPort("match");
        addOutputPort("mismatch");
    }

    // 문자열 기반 조건식
    public RuleNode(String id, String condition) {
        super(id);
        // TODO-R
        RuleExpression expression = RuleExpression.parse(condition);
        this.condition = expression::evaluate;

        addInputPort("in");
        addOutputPort("match");
        addOutputPort("mismatch");
    }

    @Override
    protected void onProcess(Message message) {
        if(condition.test(message)) {
            send("match", message);
        }else {
            send("mismatch", message);
        }
    }
}