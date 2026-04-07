package com.example.xiaozhimed;

import com.example.xiaozhimed.assistant.SeparateMemoryChatAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PromptTest {

    @Autowired
    private SeparateMemoryChatAssistant separateMemoryChatAssistant;

    /**
     * 测试SystemMessage 提示词
     */
    @Test
    void testPrompt(){
        String ans = separateMemoryChatAssistant.chat(7, "今天是几号");
        System.out.println(ans);
    }
}
