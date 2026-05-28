package com.fbp.engine.plugin;

import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * plugins/ 디렉토리 스캔, JAR 파일 탐색
 * plugins 폴더에서 JAR 파일 목록만 찾아주는 역할
 */
@Slf4j
public class PluginScanner {

    /**
     * @param pluginDirPath 플러그인 디렉토리 경로
     * @return 플러그인 JAR URL 목록
     */
    public List<URL> scanForJars(String pluginDirPath) {

        File pluginDir = new File(pluginDirPath);

        // 디렉토리 없음 -> 정상 처리
        if (!pluginDir.exists() || !pluginDir.isDirectory()) {

            log.info("플러그인 디렉토리가 존재하지 않음: {}", pluginDirPath);

            return Collections.emptyList();
        }

        // .jar 파일만 필터링
        File[] jarFiles = pluginDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));

        // 빈 디렉토리 -> 정상 처리
        if (jarFiles == null || jarFiles.length == 0) {

            log.info("로드할 플러그인 JAR 없음: {}", pluginDirPath);

            return Collections.emptyList();
        }

        List<URL> urls = new ArrayList<>();

        for (File jar : jarFiles) {
            try {
                URL url = jar.toURI().toURL();
                urls.add(url);

                log.debug("플러그인 JAR 탐색 완료: {}", jar.getName());

            } catch (MalformedURLException e) {
                log.error("JAR URL 변환 실패: {}", jar.getName(), e);
            }
        }

        return urls;
    }
}
