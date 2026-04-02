package com.example.xiaozhimed;


import com.example.xiaozhimed.bean.MyChatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;


@SpringBootTest
class MongoCrudTest {

    @Autowired
    private MongoTemplate mongoTemplate;


    /**
     * 插入文档
     */
    @Test
    void testInsert() {
        MyChatMessage myChatMessage = new MyChatMessage();
        myChatMessage.setContent("聊天记录2");
        mongoTemplate.insert(myChatMessage);
    }

    @Test
    void testFindById() {
        MyChatMessage myChatMessage = mongoTemplate.findById("69cd2f5cc649712b48fd7ec0", MyChatMessage.class);
        System.out.println(myChatMessage);
    }

    @Test
    void testUpdate() {
        Criteria criteria = Criteria.where("_id").is("1");
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("content", "新的聊天记录1");
        mongoTemplate.upsert(query, update, MyChatMessage.class);
    }

    @Test
    void testDelete() {
        Criteria criteria = Criteria.where("_id").is("1");
        mongoTemplate.remove(new Query(criteria), MyChatMessage.class);
    }

}
