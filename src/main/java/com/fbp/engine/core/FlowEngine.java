package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private final Map<String, Flow> flows;
    private State state;
    private final ExecutorService executor;

    public FlowEngine() {
        this.state = State.INITIALIZED;
        this.flows = new HashMap<>();
        this.executor = Executors.newFixedThreadPool(20);
    }

    /**
     * 플로우 등록
     */
    public void register(Flow flow) {
        flows.put(flow.getId(), flow);
        log.info("[Engine] 플로우 {} 등록됨", flow.getId());
    }

    /**
     * 플로우 실행
     */
    public void startFlow(String flowId) {
        if(!flows.containsKey(flowId)) {
            throw new IllegalArgumentException("존재하지 않는 Flow: " + flowId);
        }

        Flow flow = flows.get(flowId);
        if(flow.getFlowState() == Flow.FlowState.RUNNING) {
            log.warn("[Engine] 플로우 {} 실행 중", flowId);
            return;
        }

        List<String> errors = flow.validate();
        if(!errors.isEmpty()) {
            throw new IllegalStateException("Flow " + flowId + " validation errors: " + errors);
        }

        flow.initialize();

        for (Connection conn : flow.getConnections()) {
            executor.submit(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Message msg = conn.poll();
                        if (conn.getTarget() != null) {
                            conn.getTarget().receive(msg);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        flow.setFlowState(Flow.FlowState.RUNNING);
        this.state = State.RUNNING;
        log.info("[Engine] 플로우 {} 시작됨", flow.getId());
    }

    /**
     * 플로우 정지
     */
    public void stopFlow(String flowId) {
        if(!flows.containsKey(flowId)) {
            throw new IllegalArgumentException("존재하지 않는 flow " + flowId);
        }

        Flow flow = flows.get(flowId);
        flow.shutdown();
        flow.setFlowState(Flow.FlowState.STOPPED);
        log.info("[Engine] 플로우 '{}' 정지됨", flowId);
    }

    /**
     * 엔진에 등록된 & 실행중인 플로우 모두 정지
     */
    public void shutdown() {
        for(Flow flow : flows.values()) {
            if(flow.getFlowState() == Flow.FlowState.RUNNING) {
                flow.shutdown();
                flow.setFlowState(Flow.FlowState.STOPPED);
            }
        }
        this.state = State.STOPPED;
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    /**
     * 엔진에 등록된 플로우 리스트 출력
     */
    public void listFlows() {
        if(flows.isEmpty()) {
            log.info("등록된 플로우가 없습니다.");
            return;
        }

        int idx = 1;
        for (Flow flow : flows.values()) {
            log.info("[{}] ID : {} Status : {}", idx++, flow.getId(), flow.getFlowState());
        }
    }

    /**
     * stage1 - step8 : 간단한 CLI
     * - Scanner로 사용자 입력을 받아서 처리
     */
    public void runCLI() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("FLow Engine Started");
        boolean running = true;

        while (running) {
            System.out.print("fbp> ");
            String input = scanner.nextLine();

            if (input.isEmpty()) {
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
