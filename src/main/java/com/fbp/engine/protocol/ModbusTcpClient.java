package com.fbp.engine.protocol;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

// MODBUS TCP 프로토콜을 소켓으로 직접 구현하는 클라이언트 클래스
@Slf4j
public class ModbusTcpClient {
    // 연결 대상 Modbus 서버 정보
    private String host;
    private int port;

    // TCP 통신용 기본 I/O 스트림
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    // 요청-응답 매칭용 ID, 매 요청마다 증가
    private int transactionId = 0;

    public ModbusTcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(3000); // 3초 타임아웃

        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    public void disconnect() throws IOException {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            log.error("[ModbusTcpClient] 자원 해제 중 오류 발생, {}", e.getMessage());
        }
    }

    // 연결 상태 확인
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * FC03 (Read)
     */
    public int[] readHoldingRegisters(int unitId, int startAddress, int quantity) throws IOException, ModbusException {
        int txId = ++transactionId;

        out.write(buildMbapHeader(txId, 6, unitId)); // MBAP 헤더
        out.writeByte(0x03);
        out.writeShort(startAddress);
        out.writeShort(quantity);
        out.flush();

        readMbapHeader(txId); // 응답 헤더 검증

        int fc = in.readUnsignedByte(); // function code

        if ((fc & 0x80) != 0) {
            throw new ModbusException(0x03, in.readUnsignedByte());
        }

        int byteCount = in.readUnsignedByte();
        int[] result = new int[byteCount / 2];

        for (int i = 0; i < result.length; i++) {
            result[i] = in.readUnsignedShort();
        }

        return result;
    }

    /**
     * FC06 (Write)
     */
    public void writeSingleRegister(int unitId, int address, int value) throws IOException, ModbusException {
        int txId = ++transactionId;

        out.write(buildMbapHeader(txId, 6, unitId));
        out.writeByte(0x06);
        out.writeShort(address);
        out.writeShort(value);
        out.flush();

        readMbapHeader(txId);

        int fc = in.readUnsignedByte();

        // MSB가 1이면 Exception Response
        if ((fc & 0x80) != 0) {
            throw new ModbusException(0x06, in.readUnsignedByte());
        }

        int respAddr = in.readUnsignedShort();
        int respVal = in.readUnsignedShort();

        if (respAddr != address || respVal != value) {
            throw new ModbusException(0x06, 0x03);
        }
    }

    private byte[] buildMbapHeader(int txId, int length, int unitId) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeShort(txId); // 2 bytes
        dos.writeShort(0x0000);
        dos.writeShort(length);
        dos.writeByte(unitId);

        return baos.toByteArray();
    }

    private void readMbapHeader(int expectedTxId) throws IOException {
        int respTxId = in.readUnsignedShort();
        int respPid = in.readUnsignedShort();
        int respLen = in.readUnsignedShort();
        int respUnit = in.readUnsignedByte();

        if (respTxId != expectedTxId) {
            throw new IOException("TX mismatch");
        }
    }
}
