package com.example.xiaozhimed.config;

import com.example.xiaozhimed.store.MongoChatMemoryStore;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore embeddingStore;

    @Bean
    public ContentRetriever contentRetrieverXiaozhiPinecone() {

        // 创建一个EmbeddingStoreContentRetriever对象，用于从嵌入存储中检索
        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                                        .embeddingModel(embeddingModel)
                                        .embeddingStore(embeddingStore)
                                        .maxResults(2)
                                        .minScore(0.7)  // 只有得分大于等于0.8的结果才会返回
                                        .build();

        // 👉 包一层代理
        return query -> {
            List<Content> contents = retriever.retrieve(query);

            if (contents.isEmpty()) {
                System.out.println("❌ RAG未命中");
            } else {
                System.out.println("✅ RAG命中，条数：" + contents.size());

                for (Content c : contents) {
                    System.out.println("---- RAG内容 ----");
                    System.out.println(c); // ⭐ 关键！
                }
            }

            return contents;
        };
    }
}
