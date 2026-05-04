package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import com.fbp.engine.node.AbstractNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// FBP 엔진의 최상위 관리자
@Slf4j
public class FlowEngine {
    // 엔진 상태 관리
    public enum State {
        INITIALIZED,
        RUNNING,
        STOPPED
    }

    @Getter
    private State state;
    @Getter
    private Map<String, Flow> flows;
    private ExecutorService executor = Executors.newCachedThreadPool(); // new Thread()보다 효율적인 스레드 풀 관리

    public FlowEngine() {
        state = State.INITIALIZED;
        flows = new HashMap<>();
    }

    public void register(Flow flow) {
        flows.put(flow.getId(), flow);
        log.info("[Engine] 플로우 {} 등록됨", flow.getId());
    }

    public void startFlow(String flowId) {
        if(!flows.containsKey(flowId)) {
            throw new IllegalArgumentException("존재하지 않는 flow " + flowId);
        }
        Flow flow = flows.get(flowId);

        if(!flow.validate().isEmpty()) {
            throw new IllegalStateException();
        }

        flow.initialize();

        for(Connection conn : flow.getConnections()) {
            executor.submit(() -> {
                while(flow.getFlowState() == Flow.FlowState.RUNNING) {
                    Message msg = conn.poll();
                    if(msg != null) {
                        conn.getTarget().receive(msg);
                    }
                }
            });
        }

        this.state = State.RUNNING;

        log.info("[Engine] 플로우 '{}' 시작됨", flowId);
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
        executor.shutdownNow(); // 일괄 종료
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
