package com.fbp.engine.parser;

import java.io.InputStream;

// JSON or YAML 파일에서 플로우 정의를 읽어 자동으로 노드를 생성하고 연결
public interface FlowParser {
    FlowDefinition parse(InputStream inputStream);
}