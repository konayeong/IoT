package com.fbp.engine.registry.impl;

import com.fbp.engine.core.Node;
import com.fbp.engine.node.AlertNode;
import com.fbp.engine.registry.NodeFactory;
import java.util.Map;

public class AlertFactory implements NodeFactory {
    @Override
    public Node create(String id, Map<String, Object> config) {
        return new AlertNode(id);
    }
}
