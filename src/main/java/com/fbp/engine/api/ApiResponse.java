package com.fbp.engine.api;

import com.sun.net.httpserver.HttpExchange;
import lombok.NoArgsConstructor;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

// HTTP 응답 유틸리티 (상태 코드, JSON 직렬화)
@NoArgsConstructor
public class ApiResponse {
    // HttpExchange : 클라이언트의 HTTP 요청과 서버의 HTTP 응답을 하나로 묶어서 표현하는 객체
    public static void send(HttpExchange exchange, int statusCode, String json) throws IOException {

        exchange.getResponseHeaders().add("Content-Type", "application/json");

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}