package com.fbp.engine.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * FBP 엔진의 최상위 관리자
 * - Flow를 등록하고 관리
 * - Flow를 시작하면 내부의 모든 노드를 실행
 * - Flow를 정지하면 모든 노드를 안전하게 종료
 * - 엔진 전체의  상태를 관리
 */
@Slf4j
@Getter
public class FlowEngine {
    public enum State {
        INITIALIZED,
        RUNNING,
        STOPPED
    }

    private Map<String, Flow> flows;
    private State state;

    public FlowEngine() {
        this.state = State.INITIALIZED;
        flows = new HashMap<>();
    }

    public void register(Flow flow) {
        flows.put(flow.getId(), flow);
        log.debug("[Engine] 플로우 {} 등록됨", flow.getId());
    }

    public void startFlow(String flowId) {
        if(!flows.containsKey(flowId)) {
            throw new IllegalArgumentException("존재하지 않는 Flow: " + flowId);
        }

        Flow flow = flows.get(flowId);
        if(!flow.validate().isEmpty()) {
            throw new IllegalStateException();
        }

        flow.initialize();
        this.state = State.RUNNING;
        log.debug("[Engine] 플로우 {} 정지됨", flow.getId());
    }

    public void stopFlow(String flowId) {
        if(!flows.containsKey(flowId)) {
            throw new IllegalArgumentException("존재하지 않는 flow " + flowId);
        }
        Flow flow = flows.get(flowId);

        flow.shutdown();

        log.info("[Engine] 플로우 '{}' 정지됨", flowId);
    }

    public void shutdown() {
        for(Flow flow : flows.values()) {
            flow.shutdown();
        }
        state = State.STOPPED;
    }

    public void listFlows() {
        if(flows.isEmpty()) {
            System.out.println("등록된 플로우가 없습니다.");
            return;
        }

        int idx = 1;
        for (Flow flow : flows.values()) {
            System.out.println("[" + idx++ + "] " + flow.getId() + " " +flow.getFlowState());
        }
    }

    public void runCLI() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("FLow Engine Started");
        boolean running = true;

        while (running) {
            System.out.print("fbp> ");
            String input = scanner.nextLine();

            if(input.isEmpty()) {
                continue;
            }

            String[] parts = input.split(" ");
            String command = parts[0].trim().toLowerCase();

            switch (command) {
                // list : 등록된 플로우 목록과 상태 출력
                case "list":
                    listFlows();
                    break;
                // start <id> : 플로우 시작
                case "start":
                    if (parts.length < 2) {
                        System.out.println("flow id 필요");
                        break;
                    }
                    startFlow(parts[1]);
                    break;
                // stop <id> : 플로우 정지
                case "stop":
                    if (parts.length < 2) {
                        System.out.println("flow id 필요");
                        break;
                    }
                    stopFlow(parts[1]);
                    break;
                // exit : 엔진 종료
                case "exit":
                    shutdown();
                    System.out.println("[Engine] 엔진 종료됨");
                    running = false;
                    break;

                default:
                    System.out.println("알 수 없는 명령: " + command);
            }
        }
    }
}
