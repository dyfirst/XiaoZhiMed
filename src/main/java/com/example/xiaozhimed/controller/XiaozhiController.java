package com.example.xiaozhimed.controller;

import com.example.xiaozhimed.assistant.XiaozhiAgent;
import com.example.xiaozhimed.bean.ChatForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;

// 为 Swagger UI 标记当前控制器，方便在接口文档中定位小智相关能力。
@Tag(name = "硅谷小智")
@RestController
@RequestMapping("/xiaozhi")
public class XiaozhiController {

    // 记录接口调用过程，方便在控制台观察请求进入、模型调用和执行结果。
    private static final Logger log = LoggerFactory.getLogger(XiaozhiController.class);

    @Autowired
    private XiaozhiAgent xiaozhiAgent;

    @Operation(summary = "小智对话", description = "根据 memberId 维持会话记忆，并返回模型回复。")
    @PostMapping(value = "/chat", produces = "text/stream;charset=utf-8")
    public Flux<String> chat(@Valid @RequestBody ChatForm chatForm) {
        Long memberId = chatForm.getMemberId();
        String message = chatForm.getMessage();
        long startTime = System.currentTimeMillis();

        log.info("收到小智对话请求: memberId={}, message={}", memberId, message);

        LocalDate today = LocalDate.now();
        String[] dayNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        String dayOfWeek = dayNames[today.getDayOfWeek().getValue() - 1];
        String currentDate = today + "（" + dayOfWeek + "）";

        return xiaozhiAgent.chat(memberId, message, currentDate)
                .timeout(Duration.ofSeconds(60))
                .doOnError(e -> log.error("小智模型调用失败: memberId={}, cost={}ms",
                        memberId, System.currentTimeMillis() - startTime, e))
                .onErrorResume(e -> {
                    String errorMsg = "抱歉，AI响应异常，请稍后再试";
                    if (e.getMessage() != null && e.getMessage().contains("JsonEOF")) {
                        errorMsg = "抱歉，AI处理请求时出现问题，请重试一次";
                    }
                    return Flux.just(errorMsg);
                });
    }
}
