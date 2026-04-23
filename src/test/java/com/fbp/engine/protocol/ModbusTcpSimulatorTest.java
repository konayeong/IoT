package com.fbp.engine.protocol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModbusTcpSimulatorTest {

    private ModbusTcpSimulator simulator;

    @BeforeEach
    void setUp() {
        simulator = new ModbusTcpSimulator(5020, 10);
    }

    @Test
    @DisplayName("시작/종료")
    void startAndStop() throws IOException {
        // TODO start() 후 포트가 열리고, stop() 후 닫힘
    }

    @Test
    @DisplayName("레지스터 초기값")
    void register_init() {
        simulator.setRegister(0, 250);
        assertEquals(250, simulator.getRegister(0));
    }

    @Test
    @DisplayName("FC 03 응답")
    void response_fc03() {

    }

    @Test
    @DisplayName("FC 06 응답")
    void response_fc06() {

    }

    @Test
    @DisplayName("잘못된 주소 에러")
    void wrongAddressError() {

    }

    @Test
    @DisplayName("다중 클라이언트")
    void multiClient() {

    }
}