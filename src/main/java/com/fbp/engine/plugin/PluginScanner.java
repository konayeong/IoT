package com.fbp.engine.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// plugins/ 디렉토리 스캔, JAR 파일 탐색
// plugins 폴더에서 JAR 파일 목록만 찾아주는 역할
public class PluginScanner {
    public List<File> scan(String pluginDirPath) {
        File dir = new File(pluginDirPath);

        // 디렉토리 없음 -> 정상
        if(!dir.exists() || !dir.isDirectory()) {
            return Collections.emptyList();
        }

        File[] files = dir.listFiles((d,name) -> name.endsWith(".jar"));

        if(files == null || files.length == 0) {
            return Collections.emptyList();
        }

        List<File> jars = new ArrayList<>();
        Collections.addAll(jars, files);

        return jars;
    }
}
