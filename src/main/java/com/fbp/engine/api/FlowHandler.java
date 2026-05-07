package com.fbp.engine.api;

import com.fbp.engine.engine.FlowManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.RequiredArgsConstructor;
import java.io.IOException;

// /flows 엔드포인트 핸들러
@RequiredArgsConstructor
public class FlowHandler implements HttpHandler {

    private final FlowManager flowManager;

    @Override
    public void handle(HttpExchange exchange)
            throws IOException {

        switch (exchange.getRequestMethod()) {

            case "GET" -> getFlows(exchange);

            case "POST" -> createFlow(exchange);

            case "DELETE" -> deleteFlow(exchange);

            default -> ApiResponse.send(exchange,
                    405,
                    """
                    {"message":"Method Not Allowed"}
                    """);
        }
    }

    // 실행 중인 플로우 목록
    private void getFlows(HttpExchange exchange) throws IOException {

        String response = """
                [
                  {
                    "id":"flow1",
                    "status":"RUNNING"
                  }
                ]
                """;

        ApiResponse.send(exchange, 200, response);
    }

    private void createFlow(HttpExchange exchange) throws IOException {

        String response = """
                {
                  "id":"flow1",
                  "status":"DEPLOYED"
                }
                """;

        ApiResponse.send(exchange, 201, response);
    }

    private void deleteFlow(HttpExchange exchange)
            throws IOException {

        String path = exchange.getRequestURI().getPath();

        // /flows/flow1
        String[] parts = path.split("/");

        if (parts.length < 3) {

            ApiResponse.send(exchange,
                    400,
                    """
                    {"message":"Invalid Flow Id"}
                    """);

            return;
        }

        String flowId = parts[2];
//
//        boolean removed = flowManager.remove(flowId);
//
//        if (!removed) {
//
//            ApiResponse.send(exchange,
//                    404,
//                    """
//                    {"message":"Flow Not Found"}
//                    """);
//
//            return;
//        }
//
//        ApiResponse.send(exchange,
//                200,
//                """
//                {"message":"Flow Deleted"}
//                """);
    }
}
