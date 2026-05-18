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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
public class XiaozhiAgentConfig {

    // 医疗相关关键词，命中则走 RAG
    private static final List<String> MEDICAL_KEYWORDS = Arrays.asList(
            // 症状
            "疼", "痛", "发烧", "发热", "咳嗽", "头晕", "恶心", "呕吐", "腹泻", "出血",
            "麻木", "无力", "抽搐", "昏迷", "胸闷", "气短", "心悸", "失眠", "水肿", "皮疹",
            "瘙痒", "视力", "听力", "耳鸣", "鼻塞", "流涕",
            // 科室
            "内科", "外科", "骨科", "神经", "心脏", "消化", "呼吸", "泌尿", "眼科", "耳鼻喉",
            "皮肤", "妇产", "儿科", "肿瘤", "急诊", "中医", "口腔", "内分泌", "肾病",
            // 医疗相关
            "医生", "专家", "教授", "主任", "擅长", "看什么", "挂什么科", "什么病",
            "检查", "化验", "CT", "核磁", "B超", "X光", "血常规", "尿常规",
            "治疗", "手术", "药物", "吃药", "住院", "出院", "康复",
            // 医院相关
            "华西", "医院", "门诊", "挂号", "预约", "就诊", "看病"
    );

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

    @Bean
    public ContentRetriever contentRetrieverXiaozhiPinecone() {
        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(3)
                .minScore(0.65)
                .build();

        return query -> {
            try {
                String queryText = query.text().toLowerCase();

                // 意图路由：检查是否包含医疗关键词
                boolean isMedicalQuery = MEDICAL_KEYWORDS.stream()
                        .anyMatch(keyword -> queryText.contains(keyword));

                if (!isMedicalQuery) {
                    log.info("RAG跳过：非医疗相关查询 [{}]", query.text());
                    return Collections.emptyList();
                }

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
}
