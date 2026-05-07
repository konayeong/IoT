package com.fbp.engine.plugin;

import java.net.URL;
import java.net.URLClassLoader;

// URLClassLoader 확장 - 외부 JAR 로드
// JAR 파일을 런타임에 읽어서 클래스를 로딩하는 클래스 로더
public class PluginClassLoader extends URLClassLoader {

    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
}
