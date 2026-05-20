package com.example.xiaozhimed.store;

import com.example.xiaozhimed.bean.ChatSessionDoc;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class MongoChatMemoryStore implements ChatMemoryStore {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        try {
            String[] parts = parseMemoryId(memoryId.toString());
            String sessionId = parts[1];

            Criteria criteria = Criteria.where("sessionId").is(sessionId);
            Query query = new Query(criteria);
            ChatSessionDoc doc = mongoTemplate.findOne(query, ChatSessionDoc.class);

            if (doc == null || doc.getMessages() == null) {
                return new LinkedList<>();
            }

            return ChatMessageDeserializer.messagesFromJson(doc.getMessages());
        } catch (Exception e) {
            log.error("MongoDB读取失败，降级为无记忆模式: memoryId={}", memoryId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        try {
            String[] parts = parseMemoryId(memoryId.toString());
            Long userId = Long.parseLong(parts[0]);
            String sessionId = parts[1];

            String title = extractTitle(list);
            log.info("保存会话: sessionId={}, title={}, 消息数={}", sessionId, title, list.size());

            Criteria criteria = Criteria.where("sessionId").is(sessionId);
            Query query = new Query(criteria);
            Update update = new Update();
            update.set("messages", ChatMessageSerializer.messagesToJson(list));
            update.set("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            update.setOnInsert("userId", userId);
            update.setOnInsert("sessionId", sessionId);
            // 有有效标题时更新，否则只在创建时设置
            if (!"新会话".equals(title)) {
                update.set("title", title);
            } else {
                update.setOnInsert("title", title);
            }
            mongoTemplate.upsert(query, update, ChatSessionDoc.class);
        } catch (Exception e) {
            log.error("MongoDB写入失败，跳过记忆保存: memoryId={}", memoryId, e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        try {
            String[] parts = parseMemoryId(memoryId.toString());
            String sessionId = parts[1];

            Criteria criteria = Criteria.where("sessionId").is(sessionId);
            Query query = new Query(criteria);
            Update update = new Update();
            update.unset("messages");
            mongoTemplate.updateFirst(query, update, ChatSessionDoc.class);
        } catch (Exception e) {
            log.error("MongoDB删除失败: memoryId={}", memoryId, e);
        }
    }

    private String extractTitle(List<ChatMessage> list) {
        try {
            for (ChatMessage msg : list) {
                if (msg instanceof UserMessage userMsg) {
                    String text;
                    try {
                        text = userMsg.singleText();
                    } catch (Exception e) {
                        // singleText() 在有多条内容时会抛异常，用 toString 兜底
                        text = userMsg.toString();
                    }
                    // 去掉 RAG 上下文部分
                    int ragIndex = text.indexOf("\n\nAnswer using the following information:");
                    if (ragIndex > 0) {
                        text = text.substring(0, ragIndex);
                    }
                    text = text.trim();
                    if (text.isEmpty()) {
                        continue;
                    }
                    if (text.length() > 18) {
                        return text.substring(0, 18);
                    }
                    return text;
                }
            }
        } catch (Exception e) {
            log.warn("提取会话标题失败: {}", e.getMessage());
        }
        return "新会话";
    }

    private String[] parseMemoryId(String memoryId) {
        int idx = memoryId.indexOf(':');
        if (idx > 0) {
            return new String[]{memoryId.substring(0, idx), memoryId.substring(idx + 1)};
        }
        return new String[]{"0", memoryId};
    }
}
