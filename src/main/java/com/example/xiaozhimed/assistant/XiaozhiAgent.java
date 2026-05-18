package com.example.xiaozhimed.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
//        chatModel = "qwenChatModel",
        streamingChatModel = "qwenStreamingChatModel",  // 流式输出
        chatMemoryProvider = "chatMemoryProviderXiaozhi",
        tools = "appointmentTools",
        contentRetriever = "contentRetrieverXiaozhiPinecone"
)
public interface XiaozhiAgent {

    @SystemMessage("{{prompt_content}}")
    Flux<String> chat(@MemoryId Long memoryId,
                      @UserMessage String userMessage,
                      @V("current_date") String currentDate,
                      @V("prompt_content") String promptContent);
}
