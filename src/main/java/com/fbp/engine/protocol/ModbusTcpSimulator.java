package com.fbp.engine.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// TODO-R
// ServerSocket으로 연결을 받고, 요청 프레임을 파싱하여 레지스터 값을 돌려주는 프로그램
public class ModbusTcpSimulator {

    private final int port;
    private final int[] registers; // 레지스터 저장소

    private ServerSocket serverSocket;
    // volatile : Main Memory에 read & write를 보장하는 키워드
    private volatile boolean running = false;

    public ModbusTcpSimulator(int port, int registerCount) {
        this.port = port;
        this.registers = new int[registerCount];
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port); // 클라이언트 연결 대기
        running = true;

        Thread t = new Thread(() -> {
            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    new Thread(() -> handleClient(client)).start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        t.start();
    }

    public void stop() throws IOException {
        running = false;
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    public void handleClient(Socket socket) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            while (running) {
                // 1. MBAP 헤더 수신
                int transactionId = in.readUnsignedShort();
                int protocolId = in.readUnsignedShort();
                int length = in.readUnsignedShort();
                int unitId = in.readUnsignedByte();

                // 2. Function Code에 따라 분기
                int function = in.readUnsignedByte();

                if(function == 0x03) {
                    int startAddr = in.readUnsignedShort();
                    int quantity = in.readUnsignedShort();

                    if(startAddr + quantity > registers.length) {
                        sendException(out, transactionId, unitId, function, 0x02);
                        continue;
                    }
                    ByteArrayOutputStream body = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(body);

                    dos.writeByte(function);
                    dos.writeByte(quantity * 2);

                    for (int i = 0; i < quantity; i++) {
                        dos.writeShort(registers[startAddr + i]);
                    }

                    byte[] bodyBytes = body.toByteArray();

                    sendResponse(out, transactionId, unitId, bodyBytes);
                } else if(function == 0x06) {
                    int addr = in.readUnsignedShort();
                    int value = in.readUnsignedShort();

                    if (addr >= registers.length) {
                        sendException(out, transactionId, unitId, function, 0x02);
                        continue;
                    }

                    registers[addr] = value;

                    ByteArrayOutputStream body = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(body);

                    dos.writeByte(function);
                    dos.writeShort(addr);
                    dos.writeShort(value);

                    sendResponse(out, transactionId, unitId, body.toByteArray());
                } else {
                    sendException(out, transactionId, unitId, function, 0x01);
                }
            }
        } catch (IOException e) {
            // TODO
        }
    }

    public void setRegister(int address, int value) {
        registers[address] = value;
    }

    public int getRegister(int address) {
        return registers[address];
    }

    private void sendResponse(DataOutputStream out,
                              int tid,
                              int unitId,
                              byte[] pdu) throws IOException {

        byte[] mbap = buildMbap(tid, pdu.length + 1, unitId);

        out.write(mbap);
        out.write(pdu);
        out.flush();
    }

    private void sendException(DataOutputStream out,
                               int tid,
                               int unitId,
                               int function,
                               int code) throws IOException {

        byte[] mbap = buildMbap(tid, 3, unitId);

        out.write(mbap);
        out.writeByte(function | 0x80); // 에러 플래그
        out.writeByte(code);
        out.flush();
    }

    private byte[] buildMbap(int tid, int length, int unitId) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeShort(tid);
        dos.writeShort(0x0000); // protocol id
        dos.writeShort(length);
        dos.writeByte(unitId);

        return baos.toByteArray();
    }

}
