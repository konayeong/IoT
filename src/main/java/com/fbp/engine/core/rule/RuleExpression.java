package com.fbp.engine.core.rule;

import com.fbp.engine.message.Message;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RuleExpression {
    private final String field;
    private final String operator;
    private final Object value;

    public static RuleExpression parse(String expression) {
        String[] parts = expression.trim().split("\\s+", 3); // "\\s+" : 하나 이상의 연속된 공백을 기준으로 문자열 분리

        if (parts.length != 3) {
            throw new IllegalArgumentException("잘못된 expression: " + expression);
        }

        return new RuleExpression(parts[0], parts[1], parts[2]); // [0]=field, [1]=operator, [2]=value
    }

    public boolean evaluate(Message message) {
        Object fieldValue = message.get(field);
        if (fieldValue == null) return false;

        return switch (operator) {
            case ">", ">=", "<", "<=" -> compareNumber(fieldValue); // 숫자
            case "==" -> compareEquals(fieldValue); // 숫자 or 문자
            case "!=" -> !compareEquals(fieldValue); // 숫자 or 문자
            default -> throw new IllegalArgumentException("지원하지 않는 연산자: " + operator);
        };
    }

    // 숫자 비교
    private boolean compareNumber(Object fieldValue) {
        double left = toDouble(fieldValue);
        double right = toDouble(value);

        return switch (operator) {
            case ">" -> left > right;
            case ">=" -> left >= right;
            case "<" -> left < right;
            case "<=" -> left <= right;
            default -> false;
        };
    }

    private double toDouble(Object v) {
        if (v instanceof Number n)
            return n.doubleValue();
        return Double.parseDouble(v.toString());
    }

    // === equals 비교 (숫자 or 문자) ===
    private boolean compareEquals(Object fieldValue) {
        if (fieldValue instanceof Number || isNumeric((String) value)) {
            return toDouble(fieldValue) == toDouble(value);
        }
        return fieldValue.toString().equals(value);
    }

    private boolean isNumeric(String v) {
        try {
            Double.parseDouble(v);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
