package com.fbp.engine.runner;

import com.fbp.engine.api.HttpApiServer;
import com.fbp.engine.core.BridgeConnectionFactory;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.parser.FlowManager;
import com.fbp.engine.parser.FlowParser;
import com.fbp.engine.parser.JsonFlowParser;
import com.fbp.engine.plugin.PluginManager;
import com.fbp.engine.plugin.PluginScanner;
import com.fbp.engine.registry.NodeRegistry;

public class Stage3 {
    public static void main(String[] args) throws Exception {

        NodeRegistry nodeRegistry = new NodeRegistry();

        PluginManager pluginManager = new PluginManager(nodeRegistry, new PluginScanner());
        pluginManager.loadPlugins("plugins");

        MetricsCollector metricsCollector = new MetricsCollector();
        FlowEngine flowEngine = new FlowEngine(metricsCollector);

        FlowManager flowManager = new FlowManager(nodeRegistry, flowEngine, metricsCollector, new BridgeConnectionFactory());
        FlowParser flowParser = new JsonFlowParser();

        HttpApiServer server = new HttpApiServer(8080, flowManager, metricsCollector, flowParser);

        server.start();

        System.out.println("Server started on http://localhost:8080");
    }
}