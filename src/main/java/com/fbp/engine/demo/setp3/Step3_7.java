package com.fbp.engine.demo.setp3;

import com.fbp.engine.core.Connection;
import com.fbp.engine.node.GeneratorNode;
import com.fbp.engine.node.PrintNode;

public class Step3_7 {
    public static void run() {
        System.out.println("\n==== Step3-7 ====");
        GeneratorNode generator = new GeneratorNode("gen-1");
        Connection connection = new Connection("conn-1");

        PrintNode printNode = new PrintNode("print-1");

        connection.setTarget(printNode.getInputPort());
        generator.getOutputPort().connect(connection);

        generator.generate("temperature", 25.5);
    }
}
