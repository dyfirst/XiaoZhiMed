package com.example.xiaozhimed.controller;

import com.example.xiaozhimed.assistant.XiaozhiAgent;
import com.example.xiaozhimed.bean.ChatForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 为 Swagger UI 标记当前控制器，方便在接口文档中定位小智相关能力。
@Tag(name = "硅谷小智")
@RestController
@RequestMapping("/xiaozhi")
public class XiaozhiController {

    // 记录接口调用过程，方便在控制台观察请求进入、模型调用和执行结果。
    private static final Logger log = LoggerFactory.getLogger(XiaozhiController.class);

    @Autowired
    private XiaozhiAgent xiaozhiAgent;

    // 补充接口说明，便于在 Swagger UI 中直接调试和查看入参含义。
    @Operation(summary = "小智对话", description = "根据 memberId 维持会话记忆，并返回模型回复。")
    @PostMapping("/chat")
    public String chat(@RequestBody ChatForm chatForm) {
        Long memberId = chatForm.getMemberId();
        String message = chatForm.getMessage();
        long startTime = System.currentTimeMillis();

        log.info("收到小智对话请求: memberId={}, message={}", memberId, message);
        log.info("开始调用小智模型: memberId={}", memberId);

        try {
            String answer = xiaozhiAgent.chat(memberId, message);
            long cost = System.currentTimeMillis() - startTime;

            log.info("小智模型调用完成: memberId={}, cost={}ms, answer={}", memberId, cost, answer);
            return answer;
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;

            log.error("小智模型调用失败: memberId={}, cost={}ms", memberId, cost, e);
            throw e;
        }
    }
}
