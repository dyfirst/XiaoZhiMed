package com.example.xiaozhimed.langchain4j;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest()
class LLMTest {

    @Autowired
    private QwenChatModel qwenChatModel;

    @Test
    void chatTest() {
        String answer = qwenChatModel.chat("你是谁，你是什么模型？");
        System.out.println(answer);
    }
}
