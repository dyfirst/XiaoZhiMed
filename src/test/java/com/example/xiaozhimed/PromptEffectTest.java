package com.example.xiaozhimed;

import com.example.xiaozhimed.assistant.XiaozhiAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
public class PromptEffectTest {

    private static final String PROMPT = TestPromptSupport.loadPrompt();
    @Autowired
    private XiaozhiAgent xiaozhiAgent;

    private static final String TEST_MEMORY_ID = "777777:test-session";
    private static final String TODAY = LocalDate.now().toString();

    @Test
    public void testDepartmentRecommendation() {
        System.out.println("=".repeat(60));
        System.out.println("测试：科室推荐效果");
        System.out.println("=".repeat(60));

        String[] queries = {
                "我头疼，应该挂什么科？",
                "我头疼还发烧，应该挂什么科？",
                "我胸口闷，喘不上气",
                "我膝盖疼，上下楼梯困难"
        };

        for (int i = 0; i < queries.length; i++) {
            System.out.println("\n【测试 " + (i + 1) + "】用户: " + queries[i]);
            System.out.println("-".repeat(50));

            List<String> response = xiaozhiAgent.chat(TEST_MEMORY_ID + "-" + i, queries[i], TODAY, PROMPT)
                    .collectList().block();
            String fullResponse = String.join("", response);

            System.out.println("AI: " + fullResponse);

            // 检查是否包含免责声明
            if (fullResponse.contains("仅供参考")) {
                System.out.println("✅ 包含免责声明");
            } else {
                System.out.println("❌ 缺少免责声明");
            }
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试完成");
    }
}
