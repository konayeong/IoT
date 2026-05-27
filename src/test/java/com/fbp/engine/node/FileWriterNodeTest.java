package com.fbp.engine.node;

import com.fbp.engine.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class FileWriterNodeTest {

    private FileWriterNode node;

    @BeforeEach
    void setUp() {
        node = new FileWriterNode("file", "test.txt");
    }
    @Test
    @DisplayName("파일 생성")
    void create_file() {
        node.initialize();

        File file = new File("test.txt");
        assertTrue(file.exists());

        node.shutdown();
        file.delete();
    }

    @Test
    @DisplayName("내용 기록")
    void write() throws IOException {
        String path = "test.txt";
        FileWriterNode node = new FileWriterNode("writer", path);

        node.initialize();

        node.onProcess(new Message(Map.of("a", 1)));
        node.onProcess(new Message(Map.of("b", 2)));
        node.onProcess(new Message(Map.of("c", 3)));

        node.shutdown();

        List<String> lines = Files.readAllLines(Path.of(path));

        assertEquals(3, lines.size());

        new File(path).delete();
    }

    @Test
    @DisplayName("shutdown 후 파일 닫힘 - 예외")
    void shutdown_file_close_exception() {
        String path = "test.txt";
        FileWriterNode fwNode = new FileWriterNode("writer", path);

        fwNode.initialize();
        fwNode.onProcess(new Message(Map.of("a", 1)));

        fwNode.shutdown();

        Message message = new Message(Map.of("b", 2));

        assertThrows(RuntimeException.class, () -> {
            fwNode.onProcess(message);});

        new File(path).delete();
    }

    @Test
    @DisplayName("shutdown 후 파일 닫힘 - 기록 안됨")
    void shutdown_file_close() throws IOException {
        String path = "test.txt";
        FileWriterNode fwNode = new FileWriterNode("writer", path);

        fwNode.initialize();
        fwNode.onProcess(new Message(Map.of("a", 1)));

        fwNode.shutdown();

        // 예외 발생 확인 x
        try {
            fwNode.onProcess(new Message(Map.of("b", 2)));
        } catch (Exception ignored) {}

        List<String> lines = Files.readAllLines(Path.of(path));

        assertEquals(1, lines.size());

        new File(path).delete();
    }
}