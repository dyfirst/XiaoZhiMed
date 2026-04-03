package com.example.xiaozhimed.store;

import com.example.xiaozhimed.assistant.SeparateMemoryChatAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class MongoChatMemoryStoreTest {

    @Autowired
    private SeparateMemoryChatAssistant separateMemoryChatAssistant;

    @Test
    public void testChatMemoryStore() {
        String ans1 = separateMemoryChatAssistant.chat(1,"我是张三");
        System.out.println(ans1);
        String ans2 = separateMemoryChatAssistant.chat(1, "我是谁");
        System.out.println(ans2);

        String ans3 = separateMemoryChatAssistant.chat(2, "我是谁");
        System.out.println(ans3);
    }
}
