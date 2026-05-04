package com.fbp.engine.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

// TODO-R
// MODBUS TCP 프로토콜을 소켓으로 직접 구현하는 클라이언트 클래스
public class ModbusTcpClient {
    private String host;
    private int port;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private int transactionId = 0; // 매 요청마다 1 증가

    public ModbusTcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // TCP 소켓 연결
    public void connect() throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(3000);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    // 소켓 종료
    public void disconnect() throws IOException {
        if (socket != null) socket.close();
    }

    // 연결 상태 확인
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    // === FC 03 요청/응답 ===
    public int[] readHoldingRegisters(int unitId, int startAddress, int quantity) throws IOException, ModbusException {
        byte[] request = buildReadRequest(unitId, startAddress, quantity);

        // 소켓으로 전송
        out.write(request);
        out.flush();

        // 응답 프레임 수신 및 파싱
        readMapHeader();
        int function = in.readUnsignedByte();

        // 에러 응답이면 ModbusException 발생
        // function & 0x08 = 에러인지 확인 (에러 포함 function)
        // function & 0x7F = 원래 값 복구 (에러 비트 제거, 실제 Function Code만 꺼내는 연산)
        if((function & 0x80) != 0) {
            int exceptionCode = in.readUnsignedByte();
            throw new ModbusException(function & 0x7F, exceptionCode);
        }

        if (function != 0x03) {
            throw new ModbusException(function & 0x7F, 0x01);
        }
        int byteCnt = in.readUnsignedByte();

        // int[] 배열로 레지스터 값 반환
        int[] result = new int[byteCnt / 2];

        for(int i=0; i<result.length; i++) {
            result[i] = in.readUnsignedShort();
        }

        return result;
    }

    // FC 06 요청/응답
    public void writeSingleRegister(int unitId, int address, int value) throws IOException, ModbusException {
        byte[] request = buildWriteRequest(unitId, address, value);

        out.write(request);
        out.flush();

        // 3. 응답 프레임 수신
        readMapHeader();

        int function = in.readUnsignedByte();

        // 4. 에코백 검증 (주소, 값 일치 확인)
        if ((function & 0x80) != 0) {
            int exceptionCode = in.readUnsignedByte();
            throw new ModbusException(function & 0x7F, exceptionCode);
        }

        int respAddress = in.readUnsignedShort();
        int respValue = in.readUnsignedShort();

        // 5. 불일치 시 ModbusException 발생
        if (respAddress != address || respValue != value) {
            throw new ModbusException(0x06, 0x03); // ILLEGAL_DATA_VALUE
        }
    }

    // FC 03 요청 프레임 조립 (MBAP 헤더 + PDU)
    public byte[] buildReadRequest(int unitId, int startAddress, int quantity) throws IOException {
        int tid = transactionId++;

        ByteArrayOutputStream pdu = new ByteArrayOutputStream();
        DataOutputStream pd = new DataOutputStream(pdu);

        pd.writeByte(0x03);
        pd.writeShort(startAddress);
        pd.writeShort(quantity);

        return buildFrame(tid, unitId, pdu.toByteArray());
    }

    // FC 06 요청 프레임 조립
    public byte[] buildWriteRequest(int unitId, int address, int value) throws IOException {
        int tid = transactionId++;

        ByteArrayOutputStream pdu = new ByteArrayOutputStream();
        DataOutputStream pd = new DataOutputStream(pdu);

        pd.writeByte(0x06);
        pd.writeShort(address);
        pd.writeShort(value);

        return buildFrame(tid, unitId, pdu.toByteArray());
    }

    // 공통 프레임 조립 (MBAP + PDU)
    private byte[] buildFrame(int transactionId, int unitId, byte[] pduBytes) throws IOException {
        byte[] mbap = buildMbapHeader(transactionId, pduBytes.length + 1, unitId);

        ByteArrayOutputStream frame = new ByteArrayOutputStream();
        frame.write(mbap);
        frame.write(pduBytes);

        return frame.toByteArray();
    }

    // MBAP 헤더 7바이트를 읽어 파싱
    private byte[] buildMbapHeader(int transactionId, int length, int unitId) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeShort(transactionId);
        dos.writeShort(0x0000);
        dos.writeShort(length);
        dos.writeByte(unitId);

        return baos.toByteArray();
    }

    // 응답에서 MBAP 헤더 7바이트를 읽어 파싱 -> 다음 PDU를 읽을 위치로 커서를 이동
    private void readMapHeader() throws IOException {
        in.readUnsignedShort(); // tid
        in.readUnsignedShort(); // pid
        in.readUnsignedShort(); // length
        in.readUnsignedByte();  // unitId
    }
}
