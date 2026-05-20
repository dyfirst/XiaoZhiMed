package com.example.xiaozhimed.bean;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatForm {

    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @NotBlank(message = "消息不能为空")
    private String message;
}
