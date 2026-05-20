package com.example.xiaozhimed.controller;

import com.example.xiaozhimed.assistant.ChatOnlyAgent;
import com.example.xiaozhimed.assistant.ToolOnlyAgent;
import com.example.xiaozhimed.assistant.XiaozhiAgent;
import com.example.xiaozhimed.bean.ChatForm;
import com.example.xiaozhimed.bean.IntentRouteDecision;
import com.example.xiaozhimed.interceptor.AuthInterceptor;
import com.example.xiaozhimed.service.IntentRouteService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XiaozhiControllerTest {

    private HttpServletRequest mockRequest(Long userId) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(AuthInterceptor.USER_ID_ATTR)).thenReturn(userId);
        return request;
    }

    @Test
    void shouldRejectConcurrentRequestForSameMemoryIdAndReleaseAfterCancel() {
        XiaozhiAgent xiaozhiAgent = mock(XiaozhiAgent.class);
        ToolOnlyAgent toolOnlyAgent = mock(ToolOnlyAgent.class);
        ChatOnlyAgent chatOnlyAgent = mock(ChatOnlyAgent.class);
        IntentRouteService intentRouteService = mock(IntentRouteService.class);
        when(intentRouteService.route(anyLong(), anyString()))
                .thenReturn(new IntentRouteDecision("RAG", 1.0, "test"));
        when(xiaozhiAgent.chat(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Flux.never())
                .thenReturn(Flux.just("恢复正常"));

        XiaozhiController controller = new XiaozhiController();
        ReflectionTestUtils.setField(controller, "xiaozhiAgent", xiaozhiAgent);
        ReflectionTestUtils.setField(controller, "toolOnlyAgent", toolOnlyAgent);
        ReflectionTestUtils.setField(controller, "chatOnlyAgent", chatOnlyAgent);
        ReflectionTestUtils.setField(controller, "intentRouteService", intentRouteService);

        HttpServletRequest request = mockRequest(1001L);

        ChatForm firstRequest = new ChatForm();
        firstRequest.setSessionId("session-001");
        firstRequest.setMessage("第一条消息");

        ChatForm secondRequest = new ChatForm();
        secondRequest.setSessionId("session-001");
        secondRequest.setMessage("第二条消息");

        Disposable firstSubscription = controller.chat(firstRequest, request).subscribe();

        String concurrentResponse = controller.chat(secondRequest, request).blockFirst();
        assertEquals("上一轮对话仍在处理中，请等待回复完成后再发送下一条消息", concurrentResponse);

        firstSubscription.dispose();

        String nextResponse = controller.chat(secondRequest, request).blockFirst();
        assertEquals("恢复正常", nextResponse);
        verify(xiaozhiAgent, times(2)).chat(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldReplaceCurrentDatePlaceholderBeforeCallingAgent() {
        XiaozhiAgent xiaozhiAgent = mock(XiaozhiAgent.class);
        ToolOnlyAgent toolOnlyAgent = mock(ToolOnlyAgent.class);
        ChatOnlyAgent chatOnlyAgent = mock(ChatOnlyAgent.class);
        IntentRouteService intentRouteService = mock(IntentRouteService.class);
        when(intentRouteService.route(anyLong(), anyString()))
                .thenReturn(new IntentRouteDecision("RAG", 1.0, "test"));
        when(xiaozhiAgent.chat(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Flux.just("ok"));

        XiaozhiController controller = new XiaozhiController();
        ReflectionTestUtils.setField(controller, "xiaozhiAgent", xiaozhiAgent);
        ReflectionTestUtils.setField(controller, "toolOnlyAgent", toolOnlyAgent);
        ReflectionTestUtils.setField(controller, "chatOnlyAgent", chatOnlyAgent);
        ReflectionTestUtils.setField(controller, "intentRouteService", intentRouteService);

        HttpServletRequest request = mockRequest(2002L);

        ChatForm chatForm = new ChatForm();
        chatForm.setSessionId("session-002");
        chatForm.setMessage("今天是几号");

        controller.chat(chatForm, request).blockFirst();

        ArgumentCaptor<String> currentDateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(xiaozhiAgent).chat(anyString(), anyString(), currentDateCaptor.capture(), promptCaptor.capture());

        String currentDate = currentDateCaptor.getValue();
        String promptContent = promptCaptor.getValue();

        assertTrue(promptContent.contains(currentDate));
        assertFalse(promptContent.contains("{{current_date}}"));
    }

    @Test
    void shouldDispatchToolRouteToToolOnlyAgent() {
        XiaozhiAgent xiaozhiAgent = mock(XiaozhiAgent.class);
        ToolOnlyAgent toolOnlyAgent = mock(ToolOnlyAgent.class);
        ChatOnlyAgent chatOnlyAgent = mock(ChatOnlyAgent.class);
        IntentRouteService intentRouteService = mock(IntentRouteService.class);
        when(intentRouteService.route(anyLong(), anyString()))
                .thenReturn(new IntentRouteDecision("TOOL", 0.99, "业务操作"));
        when(toolOnlyAgent.chat(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Flux.just("tool-response"));

        XiaozhiController controller = new XiaozhiController();
        ReflectionTestUtils.setField(controller, "xiaozhiAgent", xiaozhiAgent);
        ReflectionTestUtils.setField(controller, "toolOnlyAgent", toolOnlyAgent);
        ReflectionTestUtils.setField(controller, "chatOnlyAgent", chatOnlyAgent);
        ReflectionTestUtils.setField(controller, "intentRouteService", intentRouteService);

        HttpServletRequest request = mockRequest(3003L);

        ChatForm chatForm = new ChatForm();
        chatForm.setSessionId("session-003");
        chatForm.setMessage("帮我查询我的预约记录");

        String response = controller.chat(chatForm, request).blockFirst();
        assertEquals("tool-response", response);
        verify(toolOnlyAgent).chat(anyString(), anyString(), anyString(), anyString());
        verify(xiaozhiAgent, times(0)).chat(anyString(), anyString(), anyString(), anyString());
        verify(chatOnlyAgent, times(0)).chat(anyString(), anyString(), anyString(), anyString());
    }
}
