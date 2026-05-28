package com.fbp.engine.registry.impl;

import com.fbp.engine.core.Node;
import com.fbp.engine.node.FilterNode;
import com.fbp.engine.registry.NodeFactory;
import java.util.Map;

public class FilterFactory implements NodeFactory {

    @Override
    public Node create(String id, Map<String, Object> config) {

        String field = (String) config.get("field");
        double threshold = ((Number) config.get("threshold")).doubleValue();

        return new FilterNode(id, field, threshold);
    }
}