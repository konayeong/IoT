package com.fbp.engine.registry.impl;

import com.fbp.engine.core.Node;
import com.fbp.engine.node.MqttPublisherNode;
import com.fbp.engine.registry.NodeFactory;
import java.util.Map;

public class MqttPublisherFactory implements NodeFactory {

    @Override
    public Node create(String id, Map<String, Object> config) {
        return new MqttPublisherNode(id, config);
    }
}