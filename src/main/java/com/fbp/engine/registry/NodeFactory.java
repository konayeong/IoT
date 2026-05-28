package com.fbp.engine.registry;

import com.fbp.engine.core.Node;
import java.util.Map;

// 노드 생성 함수형 인터페이스
@FunctionalInterface
public interface NodeFactory {
    Node create(String id, Map<String, Object> config);
}