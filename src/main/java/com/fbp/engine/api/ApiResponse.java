package com.fbp.engine.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;

// HTTP 응답 유틸리티 (상태 코드, JSON 직렬화)
public final class ApiResponse {

    private ApiResponse() {} // 유틸리티 클래스는 public 생성자를 가지면 안된다.

    private static final ObjectMapper mapper = new ObjectMapper();

    // HttpExchange : 클라이언트의 HTTP 요청과 서버의 HTTP 응답을 하나로 묶어서 표현하는 객체
    public static void send(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = toJsonBytes(body);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");

        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void ok(HttpExchange exchange, Object body) throws IOException {
        send(exchange, 200, body);
    }

    public static void created(HttpExchange exchange, Object body) throws IOException {
        send(exchange, 201, body);
    }

    public static void error(HttpExchange exchange, int status, String message) throws IOException {
        send(exchange, status, new ErrorResponse(message, status));
    }

    private static byte[] toJsonBytes(Object body) throws IOException {
        if (body == null) {
            return mapper.writeValueAsBytes(java.util.Map.of());
        }
        return mapper.writeValueAsBytes(body);
    }

    // 내부 에러 DTO
    public record ErrorResponse(String error, int status) {}
}