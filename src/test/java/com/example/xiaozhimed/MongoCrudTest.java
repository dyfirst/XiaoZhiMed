package com.example.xiaozhimed;

import com.example.xiaozhimed.bean.MyChatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * MongoDB 基础增删改查测试。
 * 这里只加载 Mongo 相关组件，避免把 JDBC 和整个应用上下文一起拉起。
 */
@DataMongoTest(
        properties = {
                // 使用独立测试库，避免污染正式数据
                "spring.data.mongodb.database=xiaozhimed_mongo_crud_test",
                // 显式关闭 JDBC 仓库，减少关系型数据库配置的干扰
                "spring.data.jdbc.repositories.enabled=false"
        }
)
class MongoCrudTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 插入一条聊天消息文档。
     */
    @Test
    void testInsert() {
        MyChatMessage myChatMessage = new MyChatMessage();
        myChatMessage.setContent("聊天记录2");
        mongoTemplate.insert(myChatMessage);
    }

    /**
     * 根据主键查询文档。
     * 这里的 id 需要替换成你本地数据库中真实存在的 _id。
     */
    @Test
    void testFindById() {
        MyChatMessage myChatMessage = mongoTemplate.findById("1", MyChatMessage.class);
        System.out.println(myChatMessage);
    }

    /**
     * 按条件更新文档。
     * 如果目标文档不存在，upsert 会执行插入。
     */
    @Test
    void testUpdate() {
        Criteria criteria = Criteria.where("_id").is("1");
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("content", "新的聊天记录1");
        mongoTemplate.upsert(query, update, MyChatMessage.class);
    }

    /**
     * 按主键删除文档。
     */
    @Test
    void testDelete() {
        Criteria criteria = Criteria.where("_id").is("1");
        mongoTemplate.remove(new Query(criteria), MyChatMessage.class);
    }
}
