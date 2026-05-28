package com.fbp.engine.registry.impl;

import com.fbp.engine.core.Node;
import com.fbp.engine.node.MqttSubscriberNode;
import com.fbp.engine.registry.NodeFactory;
import java.util.Map;

public class MqttSubscriberFactory implements NodeFactory {

    @Override
    public Node create(String id, Map<String, Object> config) {

        return new MqttSubscriberNode(id, config);
    }
}