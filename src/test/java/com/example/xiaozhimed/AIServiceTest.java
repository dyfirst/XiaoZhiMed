package com.example.xiaozhimed;

import com.example.xiaozhimed.assistant.Assistant;
import com.example.xiaozhimed.assistant.MemoryChatAssistant;
import com.example.xiaozhimed.assistant.SeparateMemoryChatAssistant;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AIServiceTest {

    @Autowired
    private QwenChatModel qwenChatModel;

    @Test
    public void testChat() {
        Assistant assistant = AiServices.create(Assistant.class, qwenChatModel);
        String answer = assistant.chat("hello, who are u");
        System.out.println(answer);
    }

    @Autowired
    private Assistant assistant;

    @Test
    public void testAssistant() {
        String answer1 = assistant.chat("我是丁丁");
        System.out.println(answer1);

        String answer2 = assistant.chat("我是谁？");
        System.out.println(answer2);

    }

    @Test
    public void testChatMemory() {

        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        Assistant assistant = AiServices
                .builder(Assistant.class)
                .chatModel(qwenChatModel)
                .chatMemory(chatMemory)
                .build();

        String ans1 = assistant.chat("我是dy，后面的聊天请这样叫我");
        System.out.println(ans1);
        String ans2 = assistant.chat("我是谁？");
        System.out.println(ans2);

    }

    @Autowired
    private MemoryChatAssistant memoryChatAssistant;

    @Test
    public void testChatMemory2(){
        String ans1 = memoryChatAssistant.chat("我是李白");
        System.out.println(ans1);
        String ans2 = memoryChatAssistant.chat("我是谁");
        System.out.println(ans2);
    }

    @Autowired
    private SeparateMemoryChatAssistant separateMemoryChatAssistant;

    @Test
    public void testChatMemory3(){
        String ans1 = separateMemoryChatAssistant.chat(1,"我是李四");
        System.out.println(ans1);
        String ans2 = separateMemoryChatAssistant.chat(1,"我是谁");
        System.out.println(ans2);

        //另一个用户的对话
        String ans3 = separateMemoryChatAssistant.chat(2,"我是谁");
        System.out.println(ans3);
    }

}
