package com.fbp.engine.protocol;

import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

@Slf4j
public class ModbusTcpSimulator {

    private final int port;

    private final int[] registers;
    private ServerSocket serverSocket;
    private volatile boolean running;

    public ModbusTcpSimulator(int port, int registerCount) {
        this.port = port;
        this.registers = new int[registerCount];
    }

    public void start() {
        running = true;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                log.info("Modbus Simulator started on port {}", port);

                while (running) {
                    Socket client = serverSocket.accept();
                    log.info("Client connected: {}", client.getRemoteSocketAddress());

                    new Thread(() -> handleClient(client)).start();
                }

            } catch (IOException e) {
                if (running) {
                    log.error("Server error: {}", e.getMessage());
                }
            }
        }).start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
            log.info("Modbus Simulator stopped");
        } catch (IOException e) {
            log.error("Stop error: {}", e.getMessage());
        }
    }

    private void handleClient(Socket socket) {

        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            while (running && !socket.isClosed()) {

                // =========================
                // 1. MBAP Header
                // =========================
                int txId = in.readUnsignedShort();
                int protocolId = in.readUnsignedShort();
                int length = in.readUnsignedShort();
                int unitId = in.readUnsignedByte();

                // =========================
                // 2. Function Code
                // =========================
                int fc = in.readUnsignedByte();

                // =========================
                // FC 03 - Read Holding Registers
                // =========================
                if (fc == 0x03) {

                    int startAddress = in.readUnsignedShort();
                    int quantity = in.readUnsignedShort();

                    if (startAddress < 0 ||
                            quantity <= 0 ||
                            startAddress + quantity > registers.length) {

                        sendException(out, txId, unitId, fc, 0x02);
                        continue;
                    }

                    int byteCount = quantity * 2;
                    int lengthField = 1 + 1 + byteCount; // FC + ByteCount + Data

                    // MBAP
                    out.writeShort(txId);
                    out.writeShort(protocolId);
                    out.writeShort(lengthField);
                    out.writeByte(unitId);

                    // PDU
                    out.writeByte(fc);
                    out.writeByte(byteCount);

                    for (int i = 0; i < quantity; i++) {
                        out.writeShort(registers[startAddress + i]);
                    }

                    out.flush();
                }

                // =========================
                // FC 06 - Write Single Register
                // =========================
                else if (fc == 0x06) {

                    int address = in.readUnsignedShort();
                    int value = in.readUnsignedShort();

                    if (address < 0 || address >= registers.length) {
                        sendException(out, txId, unitId, fc, 0x02);
                        continue;
                    }

                    registers[address] = value;

                    int lengthField = 1 + 4; // FC + address(2) + value(2)

                    // MBAP
                    out.writeShort(txId);
                    out.writeShort(protocolId);
                    out.writeShort(lengthField);
                    out.writeByte(unitId);

                    // PDU (echo-back)
                    out.writeByte(fc);
                    out.writeShort(address);
                    out.writeShort(value);

                    out.flush();
                }

                // =========================
                // Unsupported Function
                // =========================
                else {
                    sendException(out, txId, unitId, fc, 0x01);
                }
            }

        } catch (EOFException | SocketException e) {
            log.info("Client disconnected");
        } catch (IOException e) {
            log.error("Client error: {}", e.getMessage());
        } finally {
            try {
                if (!socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
        }
    }


    public void setRegister(int address, int value) {
        if (address >= 0 && address < registers.length) {
            registers[address] = value;
        }
    }

    public int getRegister(int address) {
        if (address >= 0 && address < registers.length) {
            return registers[address];
        }
        return -1;
    }

    private void sendException(DataOutputStream out, int txId, int unitId, int fc, int code) throws IOException {

        int protocolId = 0x0000;

        out.writeShort(txId);
        out.writeShort(protocolId);
        out.writeShort(3); // unit + fc + code
        out.writeByte(unitId);

        out.writeByte(fc | 0x80);
        out.writeByte(code);

        out.flush();
    }
}