package com.fbp.engine.protocol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModbusExceptionTest {

    private ModbusException exception;
    private static final int FUNCTIONCODE = 0x03;
    private static final int EXCEPTIONCODE = 0x01;

    @BeforeEach
    void setUp() {
        exception = new ModbusException(FUNCTIONCODE, EXCEPTIONCODE);
    }

    @Test
    @DisplayName("getMessage 포맷")
    void getMessage() {
        String message = exception.getMessage();
        assertTrue(message.contains(String.valueOf(FUNCTIONCODE)));
        assertTrue(message.contains(String.valueOf(EXCEPTIONCODE)));
    }

    @Test
    @DisplayName("getExceptionCode")
    void getExceptionCode() {
        int exceptionCode = exception.getExceptionCode();
        assertEquals(EXCEPTIONCODE, exceptionCode);
    }

    @Test
    @DisplayName("상수 값")
    void getConstant() {
        // TODO ILLEGAL_FUNCTION이 0x01, ILLEGAL_DATA_ADDRESS가 0x02 등
    }
}