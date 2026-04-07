package com.fbp.engine.demo.setp3;

import com.fbp.engine.core.Connection;
import com.fbp.engine.node.GeneratorNode;
import com.fbp.engine.node.PrintNode;

public class Step3_8 {
    public static void run() {
        // 1:N 연결 테스트
        System.out.println("\n==== Step3-8 ====");
        GeneratorNode generator = new GeneratorNode("gen-1");
        Connection connection1 = new Connection("conn-1");
        Connection connection2 = new Connection("conn-2");

        PrintNode printNode1 = new PrintNode("print-1");
        PrintNode printNode2 = new PrintNode("print-2");

        connection1.setTarget(printNode1.getInputPort());
        connection2.setTarget(printNode2.getInputPort());

        // GeneratorNode - DefaultOutputPort - connectionList에 connection 연결
        generator.getOutputPort().connect(connection1);
        generator.getOutputPort().connect(connection2);

        generator.generate("temperature", 25.5);

    }
}
