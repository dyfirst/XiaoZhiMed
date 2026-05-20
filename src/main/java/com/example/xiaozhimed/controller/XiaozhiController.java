package com.example.xiaozhimed.controller;

import com.example.xiaozhimed.assistant.ChatOnlyAgent;
import com.example.xiaozhimed.assistant.ToolOnlyAgent;
import com.example.xiaozhimed.assistant.XiaozhiAgent;
import com.example.xiaozhimed.bean.ChatForm;
import com.example.xiaozhimed.bean.IntentRouteDecision;
import com.example.xiaozhimed.entity.User;
import com.example.xiaozhimed.interceptor.AuthInterceptor;
import com.example.xiaozhimed.mapper.UserMapper;
import com.example.xiaozhimed.service.IntentRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

@Tag(name = "硅谷小智")
@RestController
@RequestMapping("/xiaozhi")
public class XiaozhiController {

    private static final Logger log = LoggerFactory.getLogger(XiaozhiController.class);
    private static final Set<String> IN_FLIGHT_MEMORY_IDS = ConcurrentHashMap.newKeySet();
    private static final String PROMPT_RESOURCE_PATH = "xiaozhi-prompt-template.txt";
    private static final String CURRENT_DATE_PLACEHOLDER = "{{current_date}}";
    private static final String USER_INFO_PLACEHOLDER = "{{user_info}}";

    @Autowired
    private XiaozhiAgent xiaozhiAgent;

    @Autowired
    private ToolOnlyAgent toolOnlyAgent;

    @Autowired
    private ChatOnlyAgent chatOnlyAgent;

    @Autowired
    private IntentRouteService intentRouteService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "小智对话", description = "根据 userId:sessionId 维持会话记忆，并返回模型回复。")
    @PostMapping(value = "/chat", produces = "text/stream;charset=utf-8")
    public Flux<String> chat(@Valid @RequestBody ChatForm chatForm, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        String sessionId = chatForm.getSessionId();
        String message = chatForm.getMessage();
        String memoryId = userId + ":" + sessionId;
        long startTime = System.currentTimeMillis();

        log.info("收到小智对话请求: memoryId={}, message={}", memoryId, message);

        if (!IN_FLIGHT_MEMORY_IDS.add(memoryId)) {
            log.warn("拒绝重复对话请求: memoryId={}, message={}", memoryId, message);
            return Flux.just("上一轮对话仍在处理中，请等待回复完成后再发送下一条消息");
        }

        LocalDate today = LocalDate.now();
        String[] dayNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        String dayOfWeek = dayNames[today.getDayOfWeek().getValue() - 1];
        String currentDate = today + "（" + dayOfWeek + "）";
        String promptContent;

        try {
            promptContent = loadPromptContent(currentDate, userId);
        } catch (IOException e) {
            IN_FLIGHT_MEMORY_IDS.remove(memoryId);
            log.error("读取系统提示词失败: resource={}", PROMPT_RESOURCE_PATH, e);
            return Flux.just("抱歉，系统提示词加载失败，请稍后再试");
        }

        IntentRouteDecision routeDecision = intentRouteService.route(userId, message);
        log.info("意图路由结果: memoryId={}, route={}, confidence={}, reason={}",
                memoryId, routeDecision.getRoute(), routeDecision.getConfidence(), routeDecision.getReason());

        Flux<String> responseFlux = buildResponseFlux(routeDecision.getRoute(), memoryId, message, currentDate, promptContent);

        return responseFlux
                .timeout(Duration.ofSeconds(60))
                .doOnError(e -> log.error("小智模型调用失败: memoryId={}, cost={}ms",
                        memoryId, System.currentTimeMillis() - startTime, e))
                .onErrorResume(e -> {
                    String errorMsg = "抱歉，AI响应异常，请稍后再试";
                    if (e.getMessage() != null && e.getMessage().contains("JsonEOF")) {
                        errorMsg = "抱歉，AI处理请求时出现问题，请重试一次";
                    }
                    return Flux.just(errorMsg);
                })
                .doFinally(signalType -> {
                    IN_FLIGHT_MEMORY_IDS.remove(memoryId);
                    log.info("对话请求结束: memoryId={}, signal={}, cost={}ms",
                            memoryId, signalType, System.currentTimeMillis() - startTime);
                });
    }

    private String loadPromptContent(String currentDate, Long userId) throws IOException {
        ClassPathResource resource = new ClassPathResource(PROMPT_RESOURCE_PATH);
        String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        String result = template.replace(CURRENT_DATE_PLACEHOLDER, currentDate);

        // 注入用户信息
        User user = userMapper.selectById(userId);
        String userInfo;
        if (user != null) {
            userInfo = "当前登录用户信息：\n"
                    + "- 用户ID：" + user.getId() + "\n"
                    + "- 姓名：" + user.getName() + "\n"
                    + "- 身份证号：" + user.getIdCard() + "\n"
                    + "预约挂号时直接使用以上信息，不需要再向用户询问姓名和身份证号。调用预约工具时传入用户ID（" + user.getId() + "）即可。";
        } else {
            userInfo = "当前用户信息未知，预约时需要询问用户的姓名和身份证号。";
        }
        result = result.replace(USER_INFO_PLACEHOLDER, userInfo);

        return result;
    }

    private Flux<String> buildResponseFlux(String route, String memoryId, String message, String currentDate, String promptContent) {
        return switch (route) {
            case "TOOL" -> toolOnlyAgent.chat(memoryId, message, currentDate, promptContent);
            case "CHAT" -> chatOnlyAgent.chat(memoryId, message, currentDate, promptContent);
            default -> xiaozhiAgent.chat(memoryId, message, currentDate, promptContent);
        };
    }
}
