package com.fbp.engine.api;

import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.parser.FlowManager;
import com.fbp.engine.parser.FlowParser;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

// HttpServer 기반 REST API 서버
@Slf4j
public class HttpApiServer {

    private final HttpServer server;

    public HttpApiServer(int port, FlowManager flowManager, MetricsCollector metricsCollector, FlowParser flowParser) throws IOException {
        // HTTP 서버 생성 (backlog 0 = default queue)
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        // thread pool (요청 병렬 처리)
        server.setExecutor(Executors.newFixedThreadPool(10));

        registerHandlers(flowManager, metricsCollector, flowParser);
    }

    private void registerHandlers(FlowManager flowManager, MetricsCollector metricsCollector, FlowParser flowParser) {

        server.createContext("/health", new HealthHandler(flowManager));

        server.createContext("/flows", new FlowHandler(flowManager, flowParser, metricsCollector));

        server.createContext("/nodes", new MetricsHandler(metricsCollector));
    }

    public void start() {
        server.start();
        log.info("HTTP API Server started on port {}", server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
        log.info("HTTP API Server stopped");
    }
}