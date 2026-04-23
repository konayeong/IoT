package com.fbp.engine.protocol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ModbusTcpClientTest {
    private ModbusTcpClient client;

    @BeforeEach
    void setUp() {
        client = new ModbusTcpClient("localhost", 5020);
    }

    @Test
    @DisplayName("FC 03 요청 프레임 조립")
    void readHoldingRegisters() throws IOException {
        byte[] frame = client.buildReadRequest(1, 0, 2);

        // function code
        assertEquals(0x03, frame[7]);

        // start address (0)
        assertEquals(0x00, frame[8]);
        assertEquals(0x00, frame[9]);

        // quantity (2)
        assertEquals(0x00, frame[10]);
        assertEquals(0x02, frame[11]);
    }

    @Test
    @DisplayName("FC 06 요청 프레임 조립")
    void writeSingleRegister() throws IOException {
        byte[] frame = client.buildWriteRequest(1, 2, 100);

        // function code
        assertEquals(0x06, frame[7]);

        // register address
        assertEquals(0x00, frame[8]);
        assertEquals(0x02, frame[9]);

        // value
        assertEquals(0x00, frame[10]);
        assertEquals(0x64, frame[11]);
    }

    @Test
    @DisplayName("MBAP 헤더 구조")
    void mbapHeader() throws IOException {
        byte[] frame = client.buildReadRequest(1, 0, 1);

        // transaction ID (0)
        assertEquals(0x00, frame[0]);
        assertEquals(0x00, frame[1]);

        // protocol ID(0x0000)
        assertEquals(0x00, frame[2]);
        assertEquals(0x00, frame[3]);

        // length (pud[5] + unitID[1])
        assertEquals(0x00, frame[4]);
        assertEquals(0x06, frame[5]);

        // unit ID (1)
        assertEquals(0x01, frame[6]);
    }

    @Test
    @DisplayName("Transaction ID 증가")
    void transactionId() throws IOException {
        byte[] f1 = client.buildReadRequest(1, 0, 1);
        byte[] f2 = client.buildReadRequest(1, 0, 1);

        DataInputStream d1 = new DataInputStream(new ByteArrayInputStream(f1));
        DataInputStream d2 = new DataInputStream(new ByteArrayInputStream(f2));

        // readUnsignedShort() : 앞에서 2바이트 읽어서 int로 변환
        int tid1 = d1.readUnsignedShort();
        int tid2 = d2.readUnsignedShort();

        assertEquals(tid1 + 1, tid2);
    }

    @Test
    @DisplayName("초기 상태")
    void init() {
        assertFalse(client.isConnected());
    }
}