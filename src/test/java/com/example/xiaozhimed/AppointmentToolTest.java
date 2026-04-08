package com.example.xiaozhimed;

import com.example.xiaozhimed.assistant.XiaozhiAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AppointmentToolTest {

    @Autowired
    private XiaozhiAgent xiaozhiAgent;

    @Test
    public void test() {
        String answer = xiaozhiAgent.chat(2L,"我想预约明天下午的神经外科");
        System.out.println(answer);
    }

}
