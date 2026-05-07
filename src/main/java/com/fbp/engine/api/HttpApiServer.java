package com.fbp.engine.api;

import com.fbp.engine.engine.FlowManager;
import com.fbp.engine.metrics.MetricsCollector;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

// HttpServer 기반 REST API 서버
public class HttpApiServer {

    private final HttpServer server;

    public HttpApiServer(int port, FlowManager flowManager, MetricsCollector metricsCollector) throws IOException {

        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/health", new HealthHandler(flowManager)); // 엔진 상태 반환

        server.createContext("/flows", new FlowHandler(flowManager));

        server.createContext("/nodes", new MetricsHandler(metricsCollector));
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}
