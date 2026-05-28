package com.fbp.engine.registry;

import com.fbp.engine.core.Node;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// 노드 타입 이름으로 노드 인스턴스를 생성할 수 있는 중앙 등록소
// FlowParser가 JSON에서 type을 읽었을 때, 이 레지스터를 통해 노드를 생성
public class NodeRegistry {

    private final Map<String, NodeFactory> factoryMap = new ConcurrentHashMap<>();

    // 팩토리 등록
    public void register(String typeName, NodeFactory factory) {
        if(typeName == null || typeName.isBlank()) {
            throw new NodeRegistryException("팩토리 등록 실패 : 타입명을 입력해주세요");
        }

        if(factory == null) {
            throw new NodeRegistryException("팩토리 등록 실패 : Factory는 null일 수 없습니다.");
        }

        factoryMap.put(typeName, factory); // 등록
    }

    // 노드 인스턴스 생성
    public Node create(String typeName, String id, Map<String, Object> config) {
        if(typeName == null || typeName.isEmpty()) {
            throw new NodeRegistryException("타입명을 입력해주세요.");
        }

        NodeFactory nodeFactory = factoryMap.get(typeName);
        if(nodeFactory == null) {
            throw new NodeRegistryException("등록되지 않은 타입명입니다 : " + typeName);
        }

        return nodeFactory.create(id, config);
    }

    public Set<String> getRegisteredTypes() {
        return Collections.unmodifiableSet(factoryMap.keySet());
    }

    public boolean isRegistered(String typeName) {
        return factoryMap.containsKey(typeName);
    }
}
