package com.fbp.engine.plugin;

import com.fbp.engine.registry.NodeRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.net.URL;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * 플러그인 관리자
 *
 * 역할:
 * - ClassPath 플러그인 로드 - JVM 시작 포함
 * - plugins/ 외부 JAR 로드 - 런타임 동적 로딩, 엔진 수정 없이 기능 추가
 * - NodeRegistry 자동 등록
 */
@Slf4j
@RequiredArgsConstructor
public class PluginManager {

    private final NodeRegistry nodeRegistry;
    private final PluginScanner pluginScanner;

    /**
     * 전체 플러그인 로드
     */
    public void loadPlugins(String pluginDirPath) {

        int loadedCount = 0;

        // 1. ClassPath 플러그인 로드
        loadedCount += loadClasspathPlugins();

        // 2. 외부 JAR 플러그인 로드
        loadedCount += loadExternalPlugins(pluginDirPath);

        log.info("플러그인 로드 완료 - 총 {}개 노드 타입 등록", loadedCount);
    }

    /**
     * ClassPath 기반 플러그인 로드
     */
    private int loadClasspathPlugins() {
        int count = 0;

        try {
            ServiceLoader<NodeProvider> loader = ServiceLoader.load(NodeProvider.class);

            for (NodeProvider provider : loader) {
                count += registerProvider(provider);
            }

        } catch (ServiceConfigurationError e) {
            log.error("ClassPath 플러그인 로드 실패", e);
        }

        return count;
    }

    /**
     * plugins/ 디렉토리 기반 외부 플러그인 로드
     */
    private int loadExternalPlugins(String pluginDirPath) {

        List<URL> jars = pluginScanner.scanForJars(pluginDirPath); // JAR 검색

        // 디렉토리 없거나 비어있어도 정상 처리
        if (jars.isEmpty()) {
            log.info("로드할 플러그인 JAR 없음: {}", pluginDirPath);

            return 0;
        }

        int count = 0;
        for (URL url : jars) {
            try {
                PluginClassLoader classLoader = new PluginClassLoader(new URL[]{url}, Thread.currentThread().getContextClassLoader()); // 생성

                ServiceLoader<NodeProvider> loader = ServiceLoader.load(NodeProvider.class, classLoader); // 외부 JAR 내부 SPI 탐색

                for (NodeProvider provider : loader) {
                    count += registerProvider(provider); // 등록
                }

                log.info("플러그인 JAR 로드 성공: {}", url);

            } catch (ServiceConfigurationError e) {
                log.error("유효하지 않은 플러그인 JAR: {}", url, e);

            } catch (Exception e) {
                log.error("플러그인 로드 실패: {}", url, e);
            }
        }

        return count;
    }

    /**
     * NodeProvider 등록
     */
    private int registerProvider(NodeProvider provider) {

        if (provider == null) {
            return 0;
        }

        List<NodeDescriptor> descriptors = provider.getNodeDescriptors(); // 플러그인이 제공하는 노드 목록

        if (descriptors == null || descriptors.isEmpty()) {
            return 0;
        }

        int count = 0;

        for (NodeDescriptor descriptor : descriptors) {

            validateDescriptor(descriptor);

            String typeName = descriptor.typeName();

            // 타입 충돌 방지
            if (nodeRegistry.isRegistered(typeName)) {
                throw new PluginException("이미 등록된 노드 타입: " + typeName);
            }

            nodeRegistry.register(typeName, descriptor.factory());

            log.info("플러그인 노드 등록 완료: {} ({})", typeName, descriptor.description());

            count++;
        }

        return count;
    }

    /**
     * Descriptor 검증
     */
    private void validateDescriptor(NodeDescriptor descriptor) {
        if (descriptor == null) {
            throw new PluginException("NodeDescriptor가 null임");
        }

        if (descriptor.typeName() == null || descriptor.typeName().isBlank()) {
            throw new PluginException("typeName 누락"
            );
        }

        if (descriptor.factory() == null) {
            throw new PluginException("NodeFactory 누락: " + descriptor.typeName());
        }
    }
}