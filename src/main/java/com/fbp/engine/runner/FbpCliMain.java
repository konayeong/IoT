package com.fbp.engine.runner;

import com.fbp.engine.cli.CliCommandController;
import com.fbp.engine.core.BridgeConnectionFactory;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.parser.FlowManager;
import com.fbp.engine.parser.FlowParser;
import com.fbp.engine.parser.JsonFlowParser;
import com.fbp.engine.plugin.PluginManager;
import com.fbp.engine.plugin.PluginScanner;
import com.fbp.engine.registry.NodeRegistry;
import java.util.Scanner;

public class FbpCliMain {

    public static void main(String[] args) {

        NodeRegistry nodeRegistry = new NodeRegistry();

        PluginManager pluginManager = new PluginManager(nodeRegistry, new PluginScanner());
        pluginManager.loadPlugins("plugins");

        MetricsCollector metricsCollector = new MetricsCollector();
        FlowEngine flowEngine = new FlowEngine(metricsCollector);

        FlowManager flowManager = new FlowManager(
                nodeRegistry,
                flowEngine,
                metricsCollector,
                new BridgeConnectionFactory()
        );
        FlowParser flowParser = new JsonFlowParser();

        CliCommandController cli = new CliCommandController(flowManager, flowParser);
        Scanner sc = new Scanner(System.in);
        System.out.println("FBP FLOW CLI STARTED");

        while (true) {
            System.out.print("fbp> ");
            String line = sc.nextLine();

            if (line.equals("exit")) break;

            try {
                cli.execute(line);
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }
}