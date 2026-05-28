package com.fbp.engine.plugin.impl;

import com.fbp.engine.node.FilterNode;
import com.fbp.engine.node.MqttPublisherNode;
import com.fbp.engine.node.MqttSubscriberNode;
import com.fbp.engine.node.ThresholdFilterNode;
import com.fbp.engine.plugin.NodeDescriptor;
import com.fbp.engine.plugin.NodeProvider;
import com.fbp.engine.registry.impl.FilterFactory;
import com.fbp.engine.registry.impl.MqttPublisherFactory;
import com.fbp.engine.registry.impl.MqttSubscriberFactory;
import java.util.List;

public class BuiltInNodeProvider implements NodeProvider {

    @Override
    public List<NodeDescriptor> getNodeDescriptors() {
        return List.of(

                new NodeDescriptor(
                        "MqttSubscriber",
                        "MQTT subscriber",
                        MqttSubscriberNode.class,
                        new MqttSubscriberFactory()
                ),

                new NodeDescriptor(
                        "Filter",
                        "filter",
                        FilterNode.class,
                        new FilterFactory()
                ),

                new NodeDescriptor(
                        "MqttPublisher",
                        "publisher",
                        MqttPublisherNode.class,
                        new MqttPublisherFactory()
                )
        );
    }
}