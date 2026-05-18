package com.example.xiaozhimed.controller;

import com.example.xiaozhimed.assistant.XiaozhiAgent;
import com.example.xiaozhimed.bean.ChatForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// 为 Swagger UI 标记当前控制器，方便在接口文档中定位小智相关能力。
@Tag(name = "硅谷小智")
@RestController
@RequestMapping("/xiaozhi")
public class XiaozhiController {

    // 记录接口调用过程，方便在控制台观察请求进入、模型调用和执行结果。
    private static final Logger log = LoggerFactory.getLogger(XiaozhiController.class);
    // 同一 memberId 在上一轮流式响应结束前禁止重入，避免工具调用消息顺序被打乱。
    private static final Set<Long> IN_FLIGHT_MEMBER_IDS = ConcurrentHashMap.newKeySet();
    private static final String PROMPT_RESOURCE_PATH = "xiaozhi-prompt-template.txt";
    private static final String CURRENT_DATE_PLACEHOLDER = "{{current_date}}";

    @Autowired
    private XiaozhiAgent xiaozhiAgent;

    @Operation(summary = "小智对话", description = "根据 memberId 维持会话记忆，并返回模型回复。")
    @PostMapping(value = "/chat", produces = "text/stream;charset=utf-8")
    public Flux<String> chat(@Valid @RequestBody ChatForm chatForm) {
        Long memberId = chatForm.getMemberId();
        String message = chatForm.getMessage();
        long startTime = System.currentTimeMillis();

        log.info("收到小智对话请求: memberId={}, message={}", memberId, message);

        if (!IN_FLIGHT_MEMBER_IDS.add(memberId)) {
            log.warn("拒绝重复对话请求: memberId={}, message={}", memberId, message);
            return Flux.just("上一轮对话仍在处理中，请等待回复完成后再发送下一条消息");
        }

        LocalDate today = LocalDate.now();
        String[] dayNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        String dayOfWeek = dayNames[today.getDayOfWeek().getValue() - 1];
        String currentDate = today + "（" + dayOfWeek + "）";
        String promptContent;

        try {
            promptContent = loadPromptContent(currentDate);
        } catch (IOException e) {
            IN_FLIGHT_MEMBER_IDS.remove(memberId);
            log.error("读取系统提示词失败: resource={}", PROMPT_RESOURCE_PATH, e);
            return Flux.just("抱歉，系统提示词加载失败，请稍后再试");
        }

        return xiaozhiAgent.chat(memberId, message, currentDate, promptContent)
                .timeout(Duration.ofSeconds(60))
                .doOnError(e -> log.error("小智模型调用失败: memberId={}, cost={}ms",
                        memberId, System.currentTimeMillis() - startTime, e))
                .onErrorResume(e -> {
                    String errorMsg = "抱歉，AI响应异常，请稍后再试";
                    if (e.getMessage() != null && e.getMessage().contains("JsonEOF")) {
                        errorMsg = "抱歉，AI处理请求时出现问题，请重试一次";
                    }
                    return Flux.just(errorMsg);
                })
                .doFinally(signalType -> {
                    IN_FLIGHT_MEMBER_IDS.remove(memberId);
                    log.info("对话请求结束: memberId={}, signal={}, cost={}ms",
                            memberId, signalType, System.currentTimeMillis() - startTime);
                });
    }

    private String loadPromptContent(String currentDate) throws IOException {
        ClassPathResource resource = new ClassPathResource(PROMPT_RESOURCE_PATH);
        String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return template.replace(CURRENT_DATE_PLACEHOLDER, currentDate);
    }
}
