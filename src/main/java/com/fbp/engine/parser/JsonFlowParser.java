package com.fbp.engine.parser;

import java.io.InputStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbp.engine.parser.definition.ConnectionDefinition;
import com.fbp.engine.parser.definition.FlowDefinition;
import com.fbp.engine.parser.definition.NodeDefinition;
import com.fbp.engine.parser.definition.TransportDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// JSON -> FlowDefinition
public class JsonFlowParser implements FlowParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FlowDefinition parse(InputStream inputStream) {
        try {
            // JsonNode : JSON 데이터를 트리 형태로 표현, 키-값 구조 -> 부모-자식 노드로 매핑
            JsonNode root = objectMapper.readTree(inputStream);

            String id = getRequiredText(root, "id");
            String name = getOptionalText(root, "name");
            String description = getOptionalText(root, "description");

            TransportDefinition transport = parseTransport(root.get("transport"));
            List<NodeDefinition> nodes = parseNodes(root.get("nodes"));
            List<ConnectionDefinition> connections = parseConnections(root.get("connections"));

            return new FlowDefinition(id, name, transport, description, nodes, connections);

        } catch (FlowParserException e) {
            throw e;
        } catch (Exception e) {
            throw new FlowParserException("JSON 파싱 실패", e);
        }
    }

    private TransportDefinition parseTransport(JsonNode transportNode) {
        if(transportNode == null) {
            return null; // TODO-R
        }

        String type = getRequiredText(transportNode, "type");
        String broker = getRequiredText(transportNode, "broker");
        int qos = Integer.parseInt(getRequiredText(transportNode, "qos"));

        return new TransportDefinition(type, broker, qos);
    }

    private List<NodeDefinition> parseNodes(JsonNode nodesNode) {
        if (nodesNode == null || !nodesNode.isArray()) {
            throw new FlowParserException("nodes must be an array");
        }

        List<NodeDefinition> nodes = new ArrayList<>();

        for (JsonNode node : nodesNode) {
            String id = getRequiredText(node, "id");
            String type = getRequiredText(node, "type");

            Map<String, Object> config = objectMapper.convertValue(
                    node.get("config"),
                    Map.class
            );

            nodes.add(new NodeDefinition(id, type, config));
        }

        if (nodes.isEmpty()) {
            throw new FlowParserException("nodes must not be empty");
        }

        return nodes;
    }

    private List<ConnectionDefinition> parseConnections(JsonNode connNode) {
        List<ConnectionDefinition> connections = new ArrayList<>();

        if (connNode == null) return connections;

        if (!connNode.isArray()) {
            throw new FlowParserException("connections must be an array");
        }

        for (JsonNode conn : connNode) {
            String from = getRequiredText(conn, "from");
            String to = getRequiredText(conn, "to");

            connections.add(new ConnectionDefinition(from, to));
        }

        return connections;
    }

    private String getRequiredText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.asText().isBlank()) {
            throw new FlowParserException("Missing required field: " + field);
        }
        return value.asText();
    }

    private String getOptionalText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null ? null : value.asText();
    }
}