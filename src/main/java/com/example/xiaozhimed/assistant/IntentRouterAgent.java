package com.example.xiaozhimed.assistant;

import com.example.xiaozhimed.bean.IntentRouteDecision;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "intentRouterChatModel"
)
public interface IntentRouterAgent {

    @SystemMessage("""
            你是医疗导诊系统的前置路由器，只负责判断当前用户消息应该走哪条链路，不负责回答用户问题。
            
            只允许输出 JSON，对象字段固定为：
            {
              "route": "TOOL|RAG|CHAT",
              "confidence": 0.0-1.0,
              "reason": "简短原因"
            }
            
            路由规则：
            1. TOOL：用户要查询/修改自己的预约、挂号、取消、确认、号源、排班等业务操作。
            2. RAG：用户在询问医院、科室、医生、挂号方式、门诊时间、症状对应科室等知识信息。
            3. CHAT：闲聊、寒暄、非医疗问题，或当前信息过少且不适合直接查知识库/工具。
            
            额外规则：
            - 用户提到“我的预约”“预约记录”“查预约”“我挂了什么号”“取消预约”“确认”等，优先判为 TOOL。
            - 用户提到“有哪些医生”“擅长什么”“门诊时间”“挂号方式”“地址”“什么科室”时，优先判为 RAG。
            - 对于“确认”“就这个”“可以”等短句，要结合上下文摘要判断；如果上下文显示正在预约或取消流程中，判为 TOOL。
            - 除 JSON 外不要输出任何解释文本。
            """)
    IntentRouteDecision classify(@UserMessage String routerInput);
}
