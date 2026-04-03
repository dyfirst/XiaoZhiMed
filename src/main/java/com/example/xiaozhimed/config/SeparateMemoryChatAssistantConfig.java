package com.example.xiaozhimed.config;

import com.example.xiaozhimed.store.MongoChatMemoryStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeparateMemoryChatAssistantConfig {

    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory
                .builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(mongoChatMemoryStore)  // 使用 MongoDB 持久化当前 memoryId 的对话窗口消息
                .build();
    }
}
