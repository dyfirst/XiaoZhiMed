package com.example.xiaozhimed.service.impl;

import com.example.xiaozhimed.assistant.IntentRouterAgent;
import com.example.xiaozhimed.bean.IntentRouteDecision;
import com.example.xiaozhimed.service.IntentRouteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class IntentRouteServiceImpl implements IntentRouteService {

    private static final List<String> TOOL_KEYWORDS = Arrays.asList(
            "我的预约", "预约记录", "查预约", "查询预约", "我挂了什么号", "我目前有哪些预约",
            "取消预约", "取消挂号", "退号", "确认预约", "确认", "帮我预约", "挂号", "号源", "排班"
    );

    private static final List<String> RAG_KEYWORDS = Arrays.asList(
            "什么科", "科室", "有哪些医生", "哪个医生", "擅长", "门诊时间", "挂号方式", "地址",
            "怎么去", "医院电话", "医院介绍", "医生介绍", "头痛", "咳嗽", "发烧", "症状"
    );

    private final IntentRouterAgent intentRouterAgent;

    public IntentRouteServiceImpl(IntentRouterAgent intentRouterAgent) {
        this.intentRouterAgent = intentRouterAgent;
    }

    @Override
    public IntentRouteDecision route(Long userId, String message) {
        String normalized = StringUtils.trimToEmpty(message);

        IntentRouteDecision ruleDecision = routeByRules(normalized);
        if (ruleDecision != null) {
            return ruleDecision;
        }

        String routerInput = buildRouterInput(userId, normalized);
        try {
            IntentRouteDecision decision = intentRouterAgent.classify(routerInput);
            if (decision == null || StringUtils.isBlank(decision.getRoute())) {
                return new IntentRouteDecision("CHAT", 0.0, "路由模型未返回有效结果");
            }
            return normalizeDecision(decision);
        } catch (Exception e) {
            log.warn("意图路由模型调用失败，降级为CHAT: userId={}", userId, e);
            return new IntentRouteDecision("CHAT", 0.0, "路由模型异常，降级为普通回答");
        }
    }

    private IntentRouteDecision routeByRules(String message) {
        if (containsAny(message, TOOL_KEYWORDS)) {
            return new IntentRouteDecision("TOOL", 0.99, "命中业务操作关键词");
        }
        if (containsAny(message, RAG_KEYWORDS)) {
            return new IntentRouteDecision("RAG", 0.95, "命中知识查询关键词");
        }
        return null;
    }

    private boolean containsAny(String message, List<String> keywords) {
        return keywords.stream().anyMatch(message::contains);
    }

    private String buildRouterInput(Long userId, String message) {
        return """
                当前用户ID：%d
                当前用户消息：
                %s
                """.formatted(userId, message);
    }

    private IntentRouteDecision normalizeDecision(IntentRouteDecision decision) {
        String route = StringUtils.upperCase(StringUtils.trimToEmpty(decision.getRoute()));
        if (!"TOOL".equals(route) && !"RAG".equals(route) && !"CHAT".equals(route)) {
            route = "CHAT";
        }
        return new IntentRouteDecision(route, decision.getConfidence(), decision.getReason());
    }
}
