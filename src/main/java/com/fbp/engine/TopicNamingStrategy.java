package com.fbp.engine;

// fbp/{flow-id}/{sourceNodeId}.{sourcePort}→{targetNodeId}.{targetPort}
public final class TopicNamingStrategy {

    private static final String PREFIX = "fbp";

    private TopicNamingStrategy() {}

    public static String build(String flowId, String sourceNodeId, String sourcePort, String targetNodeId, String targetPort) {
        validate(flowId, "flowId");
        validate(sourceNodeId, "sourceNodeId");
        validate(sourcePort, "sourcePort");
        validate(targetNodeId, "targetNodeId");
        validate(targetPort, "targetPort");

        return String.format("%s/%s/%s.%s->%s.%s", PREFIX, sanitize(flowId), sanitize(sourceNodeId), sanitize(sourcePort), sanitize(targetNodeId), sanitize(targetPort));
    }

    private static void validate(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be null or blank");
        }
    }

    private static String sanitize(String input) {
        return input.trim()
                .replace(" ", "_")
                .replace("/", "_")
                .replace("+", "_")
                .replace("#", "_");
    }
}