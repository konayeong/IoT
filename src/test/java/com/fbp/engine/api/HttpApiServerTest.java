package com.fbp.engine.api;

import com.fbp.engine.core.*;
import com.fbp.engine.message.Message;
import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.parser.FlowManager;
import com.fbp.engine.parser.JsonFlowParser;
import com.fbp.engine.registry.NodeRegistry;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpApiServerTest {

    private static HttpApiServer server;
    private static HttpClient client;
    private static int port = 18080;

    @BeforeAll
    static void setup() throws Exception {
        NodeRegistry nodeRegistry = new NodeRegistry() {

            @Override
            public Node create(String type, String id, Map<String, Object> config) {

                // ✔ 테스트에서 사용하는 노드 허용
                if (type.equals("testNode")) {
                    return new AbstractNode(id) {
                        @Override
                        protected void onProcess(Message message) {
                            // no-op (테스트용)
                        }
                    };
                }

                throw new IllegalArgumentException("Unknown node type: " + type);
            }
        };

        MetricsCollector metricsCollector = new MetricsCollector();
        FlowEngine flowEngine = new FlowEngine(metricsCollector);
        FlowManager flowManager = new FlowManager(nodeRegistry, flowEngine, metricsCollector, new BridgeConnectionFactory());
        JsonFlowParser flowParser = new JsonFlowParser();

        server = new HttpApiServer(port, flowManager, metricsCollector, flowParser);
        server.start();

        client = HttpClient.newHttpClient();
    }
    @AfterAll
    static void stop() {
        server.stop();
    }

    @Test
    @DisplayName("GET /health")
    void health_ok() throws Exception {
        HttpResponse<String> res = request("/health");

        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("status"));
        assertTrue(res.body().contains("flowSize"));
    }

    @Test
    @DisplayName("GET /flows")
    void get_flows() throws Exception {

        HttpResponse<String> res = request("/flows");

        assertEquals(200, res.statusCode());
    }

    @Test
    @DisplayName("POST /flows")
    void create_flow_success() throws Exception {

        String json = """
        {
          "id": "flow-1",
          "name": "test-flow",
          "nodes": [
            {
              "id": "n1",
              "type": "testNode",
              "config": {}
            }
          ],
          "connections": []
        }
        """;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(base() + "/flows"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, res.statusCode());
        assertTrue(res.body().contains("flow-1"));
    }

    @Test
    @DisplayName("POST /flows 잘못된 JSON")
    void create_flow_invalid() throws Exception {

        String badJson = "{ invalid }";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(base() + "/flows"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(badJson))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, res.statusCode());
    }

    @Test
    @DisplayName("DELETE /flows/{id} - success")
    void delete_flow_success() throws Exception {
        String json = """
        {
          "id": "flow-1",
          "name": "test-flow",
          "nodes": [
            {
              "id": "n1",
              "type": "testNode",
              "config": {}
            }
          ],
          "connections": []
        }
        """;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(base() + "/flows"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> deleteRes = request("/flows/flow-1", "DELETE");

        assertEquals(200, deleteRes.statusCode());
        assertTrue(deleteRes.body().contains("Flow Deleted"));
    }

    @Test
    @DisplayName("DELETE /flows/{id}")
    void delete_flow_not_found() throws Exception {
        HttpResponse<String> res = request("/flows/unknown", "DELETE");

        assertEquals(404, res.statusCode());
    }

    @Test
    @DisplayName("GET /flows/{id}/metrics")
    void flow_metrics() throws Exception {

        HttpResponse<String> res = request("/flows/flow-1/metrics");

        assertTrue(res.statusCode() == 200 || res.statusCode() == 404);
    }

    @Test
    @DisplayName("존재하지 않는 경로")
    void invalid_path() throws Exception {

        HttpResponse<String> res = request("/unknown");

        assertEquals(404, res.statusCode());
    }

    @Test
    @DisplayName("잘못된 HTTP 메서드")
    void method_not_allowed() throws Exception {

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(base() + "/health"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, res.statusCode());
    }

    // =========================================
    // helpers
    // =========================================
    private static HttpResponse<String> request(String path) throws Exception {
        return request(path, "GET");
    }

    private static HttpResponse<String> request(String path, String method) throws Exception {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(base() + path));

        if (method.equals("GET")) builder.GET();
        if (method.equals("DELETE")) builder.DELETE();

        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String base() {
        return "http://localhost:" + port;
    }
}