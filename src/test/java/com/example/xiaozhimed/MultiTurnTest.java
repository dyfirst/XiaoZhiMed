package com.example.xiaozhimed;

import com.example.xiaozhimed.assistant.XiaozhiAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
public class MultiTurnTest {

    @Autowired
    private XiaozhiAgent xiaozhiAgent;

    private static final Long TEST_USER_ID = 999999L;
    private static final String TODAY = LocalDate.now().toString();
    private static final String PROMPT = TestPromptSupport.loadPrompt();

    /**
     * 测试场景1：完整的预约挂号流程
     * 用户：我头疼 → 推荐科室 → 查询医生 → 确认预约 → 预约成功
     */
    @Test
    public void testAppointmentFlow() {
        System.out.println("=".repeat(60));
        System.out.println("测试场景1：完整的预约挂号流程");
        System.out.println("=".repeat(60));

        String[] messages = {
                "我最近总是头疼，应该挂什么科？",
                "帮我看看神经内科有哪些医生？",
                "陈永平医生明天上午有号吗？",
                "好的，帮我预约陈永平医生，我叫张三，身份证号510102199001011234"
        };

        for (int i = 0; i < messages.length; i++) {
            System.out.println("\n【第" + (i + 1) + "轮】用户: " + messages[i]);
            System.out.println("-".repeat(40));

            List<String> response = xiaozhiAgent.chat(TEST_USER_ID, messages[i], TODAY, PROMPT)
                    .collectList().block();

            String fullResponse = String.join("", response);
            System.out.println("AI: " + fullResponse);
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试场景1完成");
    }

    /**
     * 测试场景2：查询排班 → 选医生 → 预约
     * 验证排班系统是否正常工作
     */
    @Test
    public void testScheduleQuery() {
        System.out.println("=".repeat(60));
        System.out.println("测试场景2：查询排班并预约");
        System.out.println("=".repeat(60));

        // 计算下一个周一
        LocalDate nextMonday = LocalDate.now();
        while (nextMonday.getDayOfWeek().getValue() != 1) {
            nextMonday = nextMonday.plusDays(1);
        }
        String mondayStr = nextMonday.toString();

        String[] messages = {
                "骨科周一下午有哪些医生上班？",
                "付维力医生擅长什么？",
                "好的，帮我预约" + mondayStr + "下午付维力医生的号，我叫李四，身份证号510102199502022345"
        };

        for (int i = 0; i < messages.length; i++) {
            System.out.println("\n【第" + (i + 1) + "轮】用户: " + messages[i]);
            System.out.println("-".repeat(40));

            List<String> response = xiaozhiAgent.chat(TEST_USER_ID + 1, messages[i], TODAY, PROMPT)
                    .collectList().block();

            String fullResponse = String.join("", response);
            System.out.println("AI: " + fullResponse);
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试场景2完成");
    }
}
