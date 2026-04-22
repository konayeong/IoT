package com.fbp.engine.stage2.tcp;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public class EchoClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        System.out.println("[Client] 서버 연결 시도");

        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            System.out.println("[Client] 서버에 연결");

            String message = "Hello FBP";
            pw.println(message);

            String response = br.readLine();
            System.out.println("[Client] 수신 : " + response);

            pw.println("quit");
            System.out.println("[Client] 연결 종료");
        } catch (IOException e) {
            System.out.println("[Client] 연결 종료");
            log.error("[Client] 예외 발생", e);
        }
    }

}
