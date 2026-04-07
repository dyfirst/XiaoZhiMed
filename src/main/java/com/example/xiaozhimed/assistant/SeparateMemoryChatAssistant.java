package com.example.xiaozhimed.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "qwenChatModel",
        chatMemoryProvider = "chatMemoryProvider"
)

public interface SeparateMemoryChatAssistant {
//    @SystemMessage("你是我的好朋友，请用东北回答。 今天是{{current_date}}")  // 系统消息提示词
    @SystemMessage(fromResource = "my-prompt.txt")
    String chat(@MemoryId int memoryId, @UserMessage String message);
}
