package com.fbp.engine.plugin;

// 플러그인 로드/등록 실패 시 예외
public class PluginException extends RuntimeException {
    public PluginException(String message) {
        super(message);
    }
}
