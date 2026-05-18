package com.example.xiaozhimed.controller;

import com.example.xiaozhimed.assistant.ChatOnlyAgent;
import com.example.xiaozhimed.assistant.ToolOnlyAgent;
import com.example.xiaozhimed.assistant.XiaozhiAgent;
import com.example.xiaozhimed.bean.ChatForm;
import com.example.xiaozhimed.bean.IntentRouteDecision;
import com.example.xiaozhimed.service.IntentRouteService;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XiaozhiControllerTest {

    @Test
    void shouldRejectConcurrentRequestForSameMemberIdAndReleaseAfterCancel() {
        XiaozhiAgent xiaozhiAgent = Mockito.mock(XiaozhiAgent.class);
        ToolOnlyAgent toolOnlyAgent = Mockito.mock(ToolOnlyAgent.class);
        ChatOnlyAgent chatOnlyAgent = Mockito.mock(ChatOnlyAgent.class);
        IntentRouteService intentRouteService = Mockito.mock(IntentRouteService.class);
        when(intentRouteService.route(anyLong(), anyString()))
                .thenReturn(new IntentRouteDecision("RAG", 1.0, "test"));
        when(xiaozhiAgent.chat(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(Flux.never())
                .thenReturn(Flux.just("恢复正常"));

        XiaozhiController controller = new XiaozhiController();
        ReflectionTestUtils.setField(controller, "xiaozhiAgent", xiaozhiAgent);
        ReflectionTestUtils.setField(controller, "toolOnlyAgent", toolOnlyAgent);
        ReflectionTestUtils.setField(controller, "chatOnlyAgent", chatOnlyAgent);
        ReflectionTestUtils.setField(controller, "intentRouteService", intentRouteService);

        ChatForm firstRequest = new ChatForm();
        firstRequest.setMemberId(1001L);
        firstRequest.setMessage("第一条消息");

        ChatForm secondRequest = new ChatForm();
        secondRequest.setMemberId(1001L);
        secondRequest.setMessage("第二条消息");

        Disposable firstSubscription = controller.chat(firstRequest).subscribe();

        String concurrentResponse = controller.chat(secondRequest).blockFirst();
        assertEquals("上一轮对话仍在处理中，请等待回复完成后再发送下一条消息", concurrentResponse);

        firstSubscription.dispose();

        String nextResponse = controller.chat(secondRequest).blockFirst();
        assertEquals("恢复正常", nextResponse);
        verify(xiaozhiAgent, times(2)).chat(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldReplaceCurrentDatePlaceholderBeforeCallingAgent() {
        XiaozhiAgent xiaozhiAgent = Mockito.mock(XiaozhiAgent.class);
        ToolOnlyAgent toolOnlyAgent = Mockito.mock(ToolOnlyAgent.class);
        ChatOnlyAgent chatOnlyAgent = Mockito.mock(ChatOnlyAgent.class);
        IntentRouteService intentRouteService = Mockito.mock(IntentRouteService.class);
        when(intentRouteService.route(anyLong(), anyString()))
                .thenReturn(new IntentRouteDecision("RAG", 1.0, "test"));
        when(xiaozhiAgent.chat(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(Flux.just("ok"));

        XiaozhiController controller = new XiaozhiController();
        ReflectionTestUtils.setField(controller, "xiaozhiAgent", xiaozhiAgent);
        ReflectionTestUtils.setField(controller, "toolOnlyAgent", toolOnlyAgent);
        ReflectionTestUtils.setField(controller, "chatOnlyAgent", chatOnlyAgent);
        ReflectionTestUtils.setField(controller, "intentRouteService", intentRouteService);

        ChatForm request = new ChatForm();
        request.setMemberId(2002L);
        request.setMessage("今天是几号");

        controller.chat(request).blockFirst();

        ArgumentCaptor<String> currentDateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(xiaozhiAgent).chat(anyLong(), anyString(), currentDateCaptor.capture(), promptCaptor.capture());

        String currentDate = currentDateCaptor.getValue();
        String promptContent = promptCaptor.getValue();

        assertTrue(promptContent.contains(currentDate));
        assertFalse(promptContent.contains("{{current_date}}"));
    }

    @Test
    void shouldDispatchToolRouteToToolOnlyAgent() {
        XiaozhiAgent xiaozhiAgent = Mockito.mock(XiaozhiAgent.class);
        ToolOnlyAgent toolOnlyAgent = Mockito.mock(ToolOnlyAgent.class);
        ChatOnlyAgent chatOnlyAgent = Mockito.mock(ChatOnlyAgent.class);
        IntentRouteService intentRouteService = Mockito.mock(IntentRouteService.class);
        when(intentRouteService.route(anyLong(), anyString()))
                .thenReturn(new IntentRouteDecision("TOOL", 0.99, "业务操作"));
        when(toolOnlyAgent.chat(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(Flux.just("tool-response"));

        XiaozhiController controller = new XiaozhiController();
        ReflectionTestUtils.setField(controller, "xiaozhiAgent", xiaozhiAgent);
        ReflectionTestUtils.setField(controller, "toolOnlyAgent", toolOnlyAgent);
        ReflectionTestUtils.setField(controller, "chatOnlyAgent", chatOnlyAgent);
        ReflectionTestUtils.setField(controller, "intentRouteService", intentRouteService);

        ChatForm request = new ChatForm();
        request.setMemberId(3003L);
        request.setMessage("帮我查询我的预约记录");

        String response = controller.chat(request).blockFirst();
        assertEquals("tool-response", response);
        verify(toolOnlyAgent).chat(anyLong(), anyString(), anyString(), anyString());
        verify(xiaozhiAgent, times(0)).chat(anyLong(), anyString(), anyString(), anyString());
        verify(chatOnlyAgent, times(0)).chat(anyLong(), anyString(), anyString(), anyString());
    }
}
