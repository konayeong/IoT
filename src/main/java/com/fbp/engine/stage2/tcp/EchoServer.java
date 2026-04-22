package com.fbp.engine.stage2.tcp;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class EchoServer {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        System.out.println("[Server] 에코 서버 시작");

        try(ServerSocket socket = new ServerSocket(DEFAULT_PORT)) {
            while (true) {
                System.out.println("[Server] 클라이언트 대기");
                Socket client = socket.accept();
                System.out.println("[Server] 클라이언트 연결");

                handleClient(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 클라이언트와 통신
    private static void handleClient(Socket clientSocket) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String message;
            while((message = br.readLine()) != null) {
                System.out.println("[Server] 수신: " + message);

                if("quit".equalsIgnoreCase(message)) {
                    System.out.println("[Server] 종료 요청");
                    break;
                }

                pw.println(message); // 그대로 전송
            }
        } catch (IOException e) {
            log.error("[Server] 클라이언트 처리 중 오류 발생: ", e);
        }
    }
}
