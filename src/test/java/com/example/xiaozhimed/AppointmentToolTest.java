package com.example.xiaozhimed;

import com.example.xiaozhimed.assistant.XiaozhiAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class AppointmentToolTest {

    @Autowired
    private XiaozhiAgent xiaozhiAgent;
    private static final String PROMPT = TestPromptSupport.loadPrompt();

    @Test
    public void test() {
        String answer = xiaozhiAgent.chat(2L, "我想预约明天下午的神经外科", LocalDate.now().toString(), PROMPT)
                .collectList().block().toString();
        System.out.println(answer);
    }

}
