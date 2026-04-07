package com.example.xiaozhimed.config;

import com.example.xiaozhimed.store.MongoChatMemoryStore;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XiaozhiAgentConfig {

    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    @Bean
    public ChatMemoryProvider chatMemoryProviderXiaozhi() {
        // 返回一个 ChatMemoryProvider。
        // 当 LangChain4j 收到不同的 memoryId 时，会调用这个 provider 的 get(memoryId)，
        // 然后为该 memoryId 创建或读取一个对应的 ChatMemory。

        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)
                        .chatMemoryStore(mongoChatMemoryStore)
                        .build();
            }
        };
    }
}
