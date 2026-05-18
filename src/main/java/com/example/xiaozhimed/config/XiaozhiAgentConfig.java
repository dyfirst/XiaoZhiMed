package com.example.xiaozhimed.config;

import com.example.xiaozhimed.store.MongoChatMemoryStore;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
public class XiaozhiAgentConfig {

    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    @Bean
    public ChatMemoryProvider chatMemoryProviderXiaozhi() {
        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(50)
                        .chatMemoryStore(mongoChatMemoryStore)
                        .build();
            }
        };
    }

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore embeddingStore;

    @Value("${langchain4j.community.dashscope.chat-model.api-key}")
    private String dashscopeApiKey;

    @Value("${langchain4j.community.dashscope.intent-router-chat-model.model-name:qwen3.5-flash}")
    private String intentRouterModelName;

    @Value("${langchain4j.community.dashscope.intent-router-chat-model.max-tokens:512}")
    private Integer intentRouterMaxTokens;

    @Bean
    public ContentRetriever contentRetrieverXiaozhiPinecone() {
        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(3)
                .minScore(0.7)
                .build();

        return query -> {
            try {
                List<Content> contents = retriever.retrieve(query);

                if (contents.isEmpty()) {
                    log.info("RAG未命中");
                } else {
                    log.info("RAG命中，条数: {}", contents.size());
                    for (Content c : contents) {
                        log.info("RAG内容: {}", c);
                    }
                }

                return contents;
            } catch (Exception e) {
                log.error("RAG检索失败，降级为无RAG模式", e);
                return Collections.emptyList();
            }
        };
    }

    @Bean("intentRouterChatModel")
    public ChatModel intentRouterChatModel() {
        return QwenChatModel.builder()
                .apiKey(dashscopeApiKey)
                .modelName(intentRouterModelName)
                .maxTokens(Optional.ofNullable(intentRouterMaxTokens).orElse(512))
                .build();
    }
}
