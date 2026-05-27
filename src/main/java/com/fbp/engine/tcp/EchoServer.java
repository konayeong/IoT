package com.fbp.engine.tcp;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class EchoServer {
    private static final int DEFAULT_PORT = 8888;

    public static void main(String[] args) {
        log.info("[Server] 에코 서버 시작");

        try (ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {
            while (true) {
                log.info("[Server] 클라이언트 대기");
                Socket clientSocket = serverSocket.accept();
                log.info("[Server] 클라이언트 연결: {}", clientSocket.getInetAddress());

                try (BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true)
                ) {
                    String message;
                    while((message = br.readLine()) != null) {
                        log.info("[Server] 수신: {}", message);

                        if("quit".equalsIgnoreCase(message)) {
                            log.info("[Server] 종료 요청");
                            break;
                        }

                        pw.println(message); // 그대로 전송
                    }
                } catch (Exception e) {
                    log.warn("클라이언트 연결 종료 또는 에러: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[Server] 클라이언트 처리 중 오류 발생: ", e);
        }
    }
}