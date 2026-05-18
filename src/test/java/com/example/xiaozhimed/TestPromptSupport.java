package com.example.xiaozhimed;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public final class TestPromptSupport {

    private static final String PROMPT_RESOURCE_PATH = "xiaozhi-prompt-template.txt";

    private TestPromptSupport() {
    }

    public static String loadPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource(PROMPT_RESOURCE_PATH);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("读取测试提示词失败", e);
        }
    }
}
