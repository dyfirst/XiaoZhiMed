package com.example.xiaozhimed.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("chat_messages")
public class MyChatMessage {

    @Id
    private ObjectId messageId; //唯一标识，映射到 MongoDB 文档的 _Id 字段

//    private String memoryId;

    private String content;     //存储当前聊天记录列表的json字符串1
}
