package com.fbp.engine.core;

import com.fbp.engine.parser.ConnectionDefinition;
import com.fbp.engine.parser.FlowDefinition;

public interface ConnectionFactory {
    Connection create(FlowDefinition flow, ConnectionDefinition def);
}