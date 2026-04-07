package com.fbp.engine.node;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorNodeTest {

    private GeneratorNode generatorNode;

    @BeforeEach
    void setUp() {
        generatorNode = new GeneratorNode("generator-1");
    }

    @Test
    @DisplayName("generate 메시지 생성")
    void generate() {
        // generate("key", "value") 호출 시 OutputPort로 메시지가 전달됨
    }

    @Test
    @DisplayName("메시지 내용 확인")
    void check_message() {
        // 전달된 메시지의 페이로드에 지정한 key-value가 포함됨
    }

    @Test
    @DisplayName("OutputPort 조회")
    void getOutputPort() {
        assertNotNull(generatorNode.getOutputPort());
    }

    @Test
    @DisplayName("다수 generate 호출")
    void multi_generate() {

    }

}