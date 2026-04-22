package com.fbp.engine.protocol;

import lombok.Getter;

public class ModbusException extends Exception {
    private final int functionCode;
    @Getter
    private final int exceptionCode;

    private static final int ILLEGAL_FUNCTION = 0x01;
    private static final int ILLEGAL_DATA_ADDRESS = 0x02;
    private static final int ILLEGAL_DATA_VALUE = 0x03;
    private static final int SLAVE_DEVICE_FAILURE = 0x04;

    public ModbusException(int functionCode, int exceptionCode) {
        this.functionCode = functionCode;
        this.exceptionCode = exceptionCode;
    }

    @Override
    public String getMessage() {
        return String.format(
                "MODBUS 에러 — FC: 0x%02X, Exception: 0x%02X (%s)",
                functionCode,
                exceptionCode,
                getExceptionDescription(exceptionCode)
        );
    }

    private String getExceptionDescription(int code) {
        return switch (code) {
            case ILLEGAL_FUNCTION -> "Illegal Function";
            case ILLEGAL_DATA_ADDRESS -> "Illegal Data Address";
            case ILLEGAL_DATA_VALUE -> "Illegal Data Value";
            case SLAVE_DEVICE_FAILURE -> "Slave Device Failure";
            default -> "Unknown Error";
        };
    }
}
