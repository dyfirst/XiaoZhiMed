package com.example.xiaozhimed.store;

import com.example.xiaozhimed.bean.MyChatMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

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
            Criteria criteria = Criteria.where("memoryId").is(memoryId);
            Query query = new Query(criteria);
            MyChatMessage myChatMessage = mongoTemplate.findOne(query, MyChatMessage.class);

            if (myChatMessage == null) {
                return new LinkedList<>();
            }

            return ChatMessageDeserializer.messagesFromJson(myChatMessage.getContent());
        } catch (Exception e) {
            log.error("MongoDB读取失败，降级为无记忆模式: memoryId={}", memoryId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        try {
            Criteria criteria = Criteria.where("memoryId").is(memoryId);
            Query query = new Query(criteria);
            Update update = new Update();
            update.set("content", ChatMessageSerializer.messagesToJson(list));
            mongoTemplate.upsert(query, update, MyChatMessage.class);
        } catch (Exception e) {
            log.error("MongoDB写入失败，跳过记忆保存: memoryId={}", memoryId, e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        try {
            Criteria criteria = Criteria.where("memoryId").is(memoryId);
            Query query = new Query(criteria);
            mongoTemplate.remove(query, MyChatMessage.class);
        } catch (Exception e) {
            log.error("MongoDB删除失败: memoryId={}", memoryId, e);
        }
    }
}
