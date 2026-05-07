package com.fbp.engine.plugin;

import com.fbp.engine.core.Node;
import com.fbp.engine.registry.NodeFactory;

// 노드 타입 설명 레코드 (typeName, description, class, factory)
public record NodeDescriptor (
        String typeName,
        String description,
        Class<? extends Node> nodeClass,
        NodeFactory factory
){}
