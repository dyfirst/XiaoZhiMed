package com.example.xiaozhimed.assistant;

import dev.langchain4j.service.spring.AiService;

@AiService
public interface Assistant {

    String chat(String message);
}
