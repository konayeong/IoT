package com.fbp.engine.plugin;

import com.fbp.engine.registry.NodeRegistry;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ServiceLoader;

// 플러그인 검색, 로드, NodeRegistry 자동 등록
public class PluginManager {

    private final NodeRegistry nodeRegistry;

    public PluginManager(NodeRegistry nodeRegistry) {
        this.nodeRegistry = nodeRegistry;
    }

    public void loadPlugins() {
        // ClassPath 기반 로딩
        ServiceLoader<NodeProvider> pathLoader = ServiceLoader.load(NodeProvider.class);

        for(NodeProvider provider : pathLoader) {
            registerProvider(provider);
        }

        // plugins 디렉토리 로딩
        PluginScanner scanner = new PluginScanner();
        List<File> jars = scanner.scan("plugins");

        for(File jar : jars) {
            try {
                URL url = jar.toURI().toURL();
                PluginClassLoader pluginLoader = new PluginClassLoader(new URL[]{url}, getClass().getClassLoader());

                ServiceLoader<NodeProvider> serviceLoader = ServiceLoader.load(NodeProvider.class, pluginLoader);
                for(NodeProvider provider : serviceLoader) {
                    registerProvider(provider);
                }
            } catch (Exception e) {
                System.err.println("플러그인 로드 실패: " + jar.getName());
            }
        }
    }

    private void registerProvider(NodeProvider provider) {
        List<NodeDescriptor> descriptors = provider.getNodeDescriptors();

        if (descriptors == null) return;

        for (NodeDescriptor desc : descriptors) {
            if (desc.typeName() == null || desc.factory() == null) {
                throw new PluginException("잘못된 NodeDescriptor");
            }

            // NodeRegistry에 등록
            if (nodeRegistry.isRegistered(desc.typeName())) {
                throw new PluginException("타입 충돌: " + desc.typeName());
            }

            nodeRegistry.register(desc.typeName(), desc.factory());
        }
    }
}
