package com.example.xiaozhimed.bean;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatForm {

    @NotNull(message = "memberId不能为空")
    private Long memberId;

    @NotBlank(message = "消息不能为空")
    private String message;
}
