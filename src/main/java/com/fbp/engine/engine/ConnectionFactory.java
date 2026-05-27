package com.fbp.engine.engine;

import com.fbp.engine.core.conn.Connection;
import com.fbp.engine.parser.definition.ConnectionDefinition;
import com.fbp.engine.parser.definition.FlowDefinition;

public interface ConnectionFactory {
    Connection create(FlowDefinition flow, ConnectionDefinition def);
}