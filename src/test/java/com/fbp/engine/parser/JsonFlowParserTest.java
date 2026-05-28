package com.fbp.engine.parser;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

class JsonFlowParserTest {

    private final JsonFlowParser parser = new JsonFlowParser();

    @Test
    @DisplayName("정상 JSON 파싱")
    void parse_valid_json() {

        String json = """
                {
                  "id": "flow1",
                  "name": "test-flow",
                  "description": "sample",
                  "nodes": [
                    {
                      "id": "sensor",
                      "type": "mqtt-in",
                      "config": {
                        "topic": "temp",
                        "qos": 1,
                        "enabled": true
                      }
                    },
                    {
                      "id": "log",
                      "type": "log",
                      "config": {}
                    }
                  ],
                  "connections": [
                    {
                      "from": "sensor:out",
                      "to": "log:in"
                    }
                  ]
                }
                """;

        FlowDefinition definition = parse(json);

        assertEquals("flow1", definition.id());
        assertEquals("test-flow", definition.name());
        assertEquals("sample", definition.description());

        assertEquals(2, definition.nodes().size());
        assertEquals(1, definition.connections().size());
    }

    @Test
    @DisplayName("노드 목록 파싱")
    void parse_nodes() {

        String json = """
                {
                  "id": "flow1",
                  "nodes": [
                    {
                      "id": "sensor",
                      "type": "mqtt-in",
                      "config": {
                        "topic": "temp",
                        "qos": 1,
                        "enabled": true
                      }
                    }
                  ]
                }
                """;

        FlowDefinition definition = parse(json);

        NodeDefinition node = definition.nodes().get(0);

        assertEquals("sensor", node.id());
        assertEquals("mqtt-in", node.type());

        assertEquals("temp", node.config().get("topic"));
        assertEquals(1, node.config().get("qos"));
        assertEquals(true, node.config().get("enabled"));
    }

    @Test
    @DisplayName("연결 목록 파싱")
    void parse_connections() {

        String json = """
                {
                  "id": "flow1",
                  "nodes": [
                    {
                      "id": "sensor",
                      "type": "source"
                    },
                    {
                      "id": "log",
                      "type": "log"
                    }
                  ],
                  "connections": [
                    {
                      "from": "sensor:out",
                      "to": "log:in"
                    }
                  ]
                }
                """;

        FlowDefinition definition = parse(json);

        ConnectionDefinition conn =
                definition.connections().get(0);

        assertEquals("sensor", conn.fromNode());
        assertEquals("out", conn.fromPort());

        assertEquals("log", conn.toNode());
        assertEquals("in", conn.toPort());
    }

    @Test
    @DisplayName("필수 필드 누락 - id")
    void missing_flow_id() {

        String json = """
                {
                  "nodes": [
                    {
                      "id": "n1",
                      "type": "log"
                    }
                  ]
                }
                """;

        assertThrows(
                FlowParserException.class,
                () -> parse(json)
        );
    }

    @Test
    @DisplayName("필수 필드 누락 - nodes")
    void missing_nodes() {

        String json = """
                {
                  "id": "flow1"
                }
                """;

        assertThrows(
                FlowParserException.class,
                () -> parse(json)
        );
    }

    @Test
    @DisplayName("빈 노드 목록")
    void empty_nodes() {

        String json = """
                {
                  "id": "flow1",
                  "nodes": []
                }
                """;

        assertThrows(
                FlowParserException.class,
                () -> parse(json)
        );
    }

    @Test
    @DisplayName("잘못된 JSON 형식")
    void invalid_json() {

        String json = """
                {
                  "id": "flow1",
                  "nodes": [
                """;

        assertThrows(
                FlowParserException.class,
                () -> parse(json)
        );
    }

    @Test
    @DisplayName("연결 포트 파싱")
    void parse_connection_ports() {

        String json = """
                {
                  "id": "flow1",
                  "nodes": [
                    {
                      "id": "sensor",
                      "type": "source"
                    },
                    {
                      "id": "processor",
                      "type": "rule"
                    }
                  ],
                  "connections": [
                    {
                      "from": "sensor:out",
                      "to": "processor:input"
                    }
                  ]
                }
                """;

        FlowDefinition definition = parse(json);

        ConnectionDefinition conn =
                definition.connections().get(0);

        assertEquals("sensor", conn.fromNode());
        assertEquals("out", conn.fromPort());

        assertEquals("processor", conn.toNode());
        assertEquals("input", conn.toPort());
    }

    @Test
    @DisplayName("잘못된 연결 형식")
    void invalid_connection_format() {

        String json = """
                {
                  "id": "flow1",
                  "nodes": [
                    {
                      "id": "sensor",
                      "type": "source"
                    },
                    {
                      "id": "log",
                      "type": "log"
                    }
                  ],
                  "connections": [
                    {
                      "from": "sensor",
                      "to": "log:in"
                    }
                  ]
                }
                """;

        assertThrows(
                FlowParserException.class,
                () -> parse(json)
        );
    }

    @Test
    @DisplayName("존재하지 않는 노드 참조")
    void unknown_node_reference() {

        String json = """
                {
                  "id": "flow1",
                  "nodes": [
                    {
                      "id": "sensor",
                      "type": "source"
                    }
                  ],
                  "connections": [
                    {
                      "from": "sensor:out",
                      "to": "unknown:in"
                    }
                  ]
                }
                """;

        assertThrows(FlowParserException.class, () -> parse(json));
    }

    @Test
    @DisplayName("중복 노드 ID")
    void duplicate_node_id() {

        String json = """
                {
                  "id": "flow1",
                  "nodes": [
                    {
                      "id": "node1",
                      "type": "log"
                    },
                    {
                      "id": "node1",
                      "type": "rule"
                    }
                  ]
                }
                """;

        assertThrows(FlowParserException.class, () -> parse(json));
    }

    @Test
    @DisplayName("config 타입 보존")
    void config_type_preserved() {

        String json = """
                {
                  "id": "flow1",
                  "nodes": [
                    {
                      "id": "node1",
                      "type": "test",
                      "config": {
                        "stringValue": "hello",
                        "intValue": 10,
                        "doubleValue": 3.14,
                        "booleanValue": true
                      }
                    }
                  ]
                }
                """;

        FlowDefinition definition = parse(json);

        Map<String, Object> config =
                definition.nodes().get(0).config();

        assertInstanceOf(
                String.class,
                config.get("stringValue")
        );

        assertInstanceOf(
                Integer.class,
                config.get("intValue")
        );

        assertInstanceOf(
                Double.class,
                config.get("doubleValue")
        );

        assertInstanceOf(
                Boolean.class,
                config.get("booleanValue")
        );
    }

    private FlowDefinition parse(String json) {

        return parser.parse(
                new ByteArrayInputStream(
                        json.getBytes(StandardCharsets.UTF_8)
                )
        );
    }
}