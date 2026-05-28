package com.fbp.engine.node.stage2;

import com.fbp.engine.message.Message;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class EchoProtocolNode extends ProtocolNode {

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private final String host;
    private final int port;

    public EchoProtocolNode(String id, Map<String, Object> config) {
        super(id, config);

        this.host = (String) config.getOrDefault("host", "localhost");
        this.port = ((Number) config.getOrDefault("port", 8888)).intValue();
    }

    @Override
    protected void connect() throws IOException {
        socket = new Socket(host, port);

        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    protected void disconnect() throws IOException {
        if (reader != null) reader.close();
        if (writer != null) writer.close();
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        reader = null;
        writer = null;
        socket = null;
    }

    @Override
    protected void onProcess(Message message) {
        sendTestMessage(message.getPayload().toString());
    }

    public void sendTestMessage(String msg) {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected");
        }

        try {
            writer.println(msg);
            String response = reader.readLine();
            System.out.println("[EchoProtocolNode] response = " + response);
        } catch (IOException e) {
            reconnect();
        }
    }
}