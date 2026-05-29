package com.fbp.engine.cli;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.parser.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class CliCommandController {

    private final FlowManager flowManager;
    private final FlowParser flowParser;

    public CliCommandController(FlowManager manager, FlowParser flowParser) {
        this.flowManager = manager;
        this.flowParser = flowParser;
    }

    public void execute(String input) {
        String[] args = input.trim().split(" ");
        if (args.length < 2) {
            System.out.println("invalid command");
            return;
        }

        if (!args[0].equals("flow")) {
            System.out.println("only flow commands supported");
            return;
        }

        String cmd = args[1];

        switch (cmd) {
            // ---------------- LIST ----------------
            case "list" ->
                flowManager.list().forEach(flow ->
                        System.out.println(flow.getId() + " " + flow.getFlowState()));

            // ---------------- DEPLOY ----------------
            case "deploy" -> {
                if (args.length < 3) {
                    System.out.println("usage: flow deploy <file>");
                    return;
                }

                String filePath = args[2];

                try (InputStream is = new FileInputStream(filePath)) {
                    FlowDefinition def = flowParser.parse(is);
                    Flow flow = flowManager.deploy(def);

                    System.out.println("Flow deployed: " + flow.getId());
                    System.out.println("State: " + flow.getFlowState());
                    System.out.println("Nodes: " + flow.getNodes().size());
                    System.out.println("Connections: " + flow.getConnections().size());

                } catch (Exception e) {
                    System.out.println("deploy failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // ---------------- START ----------------
            case "start" -> {
                flowManager.restart(args[2]);
                System.out.println("flow started: " + args[2]);
            }

            // ---------------- STOP ----------------
            case "stop" -> {
                flowManager.stop(args[2]);
                System.out.println("flow stopped: " + args[2]);
            }

            // ---------------- RESTART ----------------
            case "restart" -> {
                flowManager.stop(args[2]);
                flowManager.restart(args[2]);
                System.out.println("flow restarted: " + args[2]);
            }

            // ---------------- REMOVE ----------------
            case "remove" -> {
                flowManager.remove(args[2]);
                System.out.println("flow removed: " + args[2]);
            }

            // ---------------- STATUS ----------------
            case "status" -> {
                Flow flow = flowManager.getFlow(args[2]);

                System.out.printf(
                        """
                                Flow: %s
                                State: %s
                                Nodes: %d
                                Connections: %d
                                %n""", flow.getId(),
                flow.getFlowState(),
                flow.getNodes().size(),
                flow.getConnections().size()
        );
            }

            default -> System.out.println("unknown flow command: " + cmd);
        }
    }
}
