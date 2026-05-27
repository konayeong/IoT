package com.fbp.engine.runner.stage2;

import com.fbp.engine.protocol.ModbusException;
import com.fbp.engine.protocol.ModbusTcpClient;
import com.fbp.engine.protocol.ModbusTcpSimulator;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

/**
 * 시뮬레이터: 포트 5020에서 동작. 레지스터 10개. 초기값: [250, 600, 1, 0, 0, 0, 0, 0, 0, 0]
 * 클라이언트: 시뮬레이터에 연결하여 주소 0~2 레지스터 읽기 → 출력 → 주소 2에 값 100 쓰기 → 다시 읽어서 변경 확인
 */
@Slf4j
public class Step3_5 {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 시뮬레이터
        ModbusTcpSimulator simulator = new ModbusTcpSimulator(5020, 10);

        // 초기값
        int[] init = {250, 600, 1, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < init.length; i++) {
            simulator.setRegister(i, init[i]);
        }

        simulator.start();
        log.debug("Simulator Start");

        Thread.sleep(5000);

        // 클라이언트
        ModbusTcpClient client = new ModbusTcpClient("localhost", 5020);
        client.connect();
        log.debug("Client Connected");

        try {
            int[] values = client.readHoldingRegisters(1, 0, 3);
            System.out.println("before values");
            print(values);

            client.writeSingleRegister(1, 2, 100);

            int[] valuesAfter = client.readHoldingRegisters(1, 0, 3);
            System.out.println("after values");
            print(valuesAfter);
        } catch (ModbusException e) {
            throw new RuntimeException(e);
        }
    }
    private static void print(int[] values) {
        for(int i : values) {
            System.out.println(i + " ");
        }
    }
}
