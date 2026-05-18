package com.example.xiaozhimed.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        streamingChatModel = "qwenStreamingChatModel",
        chatMemoryProvider = "chatMemoryProviderXiaozhi"
)
public interface ChatOnlyAgent {

    @SystemMessage("{{prompt_content}}")
    Flux<String> chat(@MemoryId Long memoryId,
                      @UserMessage String userMessage,
                      @V("current_date") String currentDate,
                      @V("prompt_content") String promptContent);
}
