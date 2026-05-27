package com.fbp.engine.node;

import com.fbp.engine.core.ConnectionState;
import com.fbp.engine.core.ProtocolNode;
import com.fbp.engine.message.Message;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

@Slf4j
public class EchoProtocolNode extends ProtocolNode {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final String host;
    private final int port;

    public EchoProtocolNode(String id, Map<String, Object> config) {
        super(id, config);

        this.host = (String) config.getOrDefault("host", "localhost");
        this.port = ((Number) config.getOrDefault("port", 8888)).intValue();
    }

    @Override
    protected void connect() throws Exception { // TCP 소켓 연결
        socket = new Socket(host, port);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        log.info("[{}] Echo 서버 연결 성공 {}:{}", getId(), host, port);
    }

    @Override
    protected void disconnect() { // 소켓 해제
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();

            log.info("[{}] Echo 서버 연결 종료", getId());
        } catch (Exception e) {
            log.error("[{}] disconnect 오류: {}", getId(), e.getMessage());
        }
    }

    @Override
    protected void onProcess(Message message) {

        if (!isConnected()) {
            log.warn("[{}] 연결 안됨 → 메시지 폐기: {}", getId(), message);
            return;
        }

        try {
            String payload = message.toString();

            out.println(payload);
            log.debug("[{}] send → {}", getId(), payload);

            String response = in.readLine();

            if (response == null) {
                throw new RuntimeException("서버 연결 종료");
            }

            log.debug("[{}] recv ← {}", getId(), response);

            send("out", new Message(Map.of("echo", response)));

        } catch (Exception e) {
            log.error("[{}] 통신 오류: {}", getId(), e.getMessage());

            // ProtocolNode의 재연결 시스템 사용
            connectionState = ConnectionState.ERROR;
            disconnect();
            reconnect();
        }
    }
}
