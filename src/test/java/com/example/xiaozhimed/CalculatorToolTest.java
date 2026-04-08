package com.example.xiaozhimed;

import com.example.xiaozhimed.assistant.SeparateMemoryChatAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest()
public class CalculatorToolTest {

    @Autowired
    private SeparateMemoryChatAssistant separateMemoryChatAssistant;

    @Test
    public void testCalculatorTools(){
        String answer = separateMemoryChatAssistant.chat(3333, "15843624+256的平方根是多少");
        System.out.println(answer);
    }

}
