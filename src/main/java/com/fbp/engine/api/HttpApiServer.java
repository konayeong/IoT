package com.fbp.engine.api;

import com.fbp.engine.engine.FlowManager;
import com.fbp.engine.metrics.MetricsCollector;
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

        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        // 요청 처리 스레드 풀
        server.setExecutor(Executors.newCachedThreadPool());

        server.createContext("/health", new HealthHandler(flowManager)); // 엔진 상태 반환

        // TODO-R
        server.createContext("/flows", new FlowHandler(flowManager, flowParser, new MetricsHandler(metricsCollector)));

        server.createContext("/nodes", new MetricsHandler(metricsCollector));
    }

    public void start() {
        server.start();
        log.debug("HTTP API Server started");
    }

    public void stop() {
        server.stop(0);
        log.debug("HTTP API Server stopped");
    }
}
