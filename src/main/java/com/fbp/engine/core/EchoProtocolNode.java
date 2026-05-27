package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class EchoProtocolNode extends ProtocolNode {

    private Socket socket;
    private final String host;
    private final int port;

    public EchoProtocolNode(String id, Map<String, Object> config) {
        super(id, config);

        this.host = (String) config.getOrDefault("host", "localhost");
        this.port = ((Number) config.getOrDefault("port", 8888)).intValue();
    }

    @Override
    protected void connect() throws IOException {
        // TCP 소켓 연결
        socket = new Socket(host, port);
    }

    @Override
    protected void disconnect() throws IOException {
        // 소켓 해제
        if(socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    @Override
    protected void onProcess(Message message) {
        // non
    }

    public void sendTestMessage(String msg) {
        try {
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            pw.println(msg);

            String response = br.readLine();
            System.out.println("[EchoProtocolNode] response = " + response);

            pw.println("quit");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
