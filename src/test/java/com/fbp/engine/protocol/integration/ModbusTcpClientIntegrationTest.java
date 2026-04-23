package com.fbp.engine.protocol.integration;

import com.fbp.engine.protocol.ModbusException;
import com.fbp.engine.protocol.ModbusTcpClient;
import com.fbp.engine.protocol.ModbusTcpSimulator;
import org.junit.jupiter.api.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

// TODO 실행할 때 발생하는 에러 확인하기
// TODO 이것도 @Tag를 이용해야 하는건지?
class ModbusTcpClientIntegrationTest {

    private static final int PORT = 5020;
    private static ModbusTcpSimulator simulator;
    private ModbusTcpClient client;

    @BeforeAll
    static void setUpServer() throws IOException {
        simulator = new ModbusTcpSimulator(PORT, 100);
        simulator.start();
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        simulator.stop();
    }

    @BeforeEach
    void setUp() throws IOException {
        client = new ModbusTcpClient("localhost", PORT);
        client.connect();
    }

    @AfterEach
    void tearDown() throws IOException {
        client.disconnect();
    }

    @Test
    @DisplayName("연결/해제")
    void connection() {
        assertTrue(client.isConnected());

        assertDoesNotThrow(() -> client.disconnect());

        assertFalse(client.isConnected());
    }

    @Test
    @DisplayName("Holding Register 읽기")
    void read_holding_register() throws IOException, ModbusException {
        simulator.setRegister(0, 123);

        int[] result = client.readHoldingRegisters(1, 0, 1);

        assertEquals(1, result.length);
        assertEquals(123, result[0]);
    }

    @Test
    @DisplayName("다수 레지스터 읽기")
    void read_multi_register() throws IOException, ModbusException {
        simulator.setRegister(0, 123);
        simulator.setRegister(1, 456);
        simulator.setRegister(2, 789);
        simulator.setRegister(3, 1234);
        simulator.setRegister(4, 5678);

        int[] result = client.readHoldingRegisters(1, 0, 5);

        assertEquals(5, result.length);
        assertEquals(123, result[0]);
        assertEquals(456, result[1]);
        assertEquals(789, result[2]);
        assertEquals(1234, result[3]);
        assertEquals(5678, result[4]);
    }

    @Test
    @DisplayName("쓰기 후 읽기")
    void writeRead() {

    }

    @Test
    @DisplayName("에러 응답 처리")
    void modbusException() {

    }

    @Test
    @DisplayName("소켓 타임아웃")
    void socketTimeOut() {
    }

}