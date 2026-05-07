package com.fbp.engine.plugin;

import java.util.List;

// SPI (서비스 제공자 인터페이스)
public interface NodeProvider {
    List<NodeDescriptor> getNodeDescriptors();
}