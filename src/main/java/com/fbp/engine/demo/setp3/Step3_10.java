package com.fbp.engine.demo.setp3;

import com.fbp.engine.core.Connection;
import com.fbp.engine.node.FilterNode;
import com.fbp.engine.node.GeneratorNode;
import com.fbp.engine.node.PrintNode;

public class Step3_10 {
    public static void run() {
        GeneratorNode generatorNode = new GeneratorNode("gen-1");
        FilterNode filterNode = new FilterNode("filter-1", "temperature", 30);
        PrintNode printNode = new PrintNode("print-1");

        Connection connection1 = new Connection("conn-1");
        Connection connection2 = new Connection("conn-2");

        // 연결
        connection1.setTarget(filterNode.getInputPort());
        connection2.setTarget(printNode.getInputPort());

        generatorNode.getOutputPort().connect(connection1);
        filterNode.getOutputPort().connect(connection2);

        generatorNode.generate("temperature", 35.0);
    }
}