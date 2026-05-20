package com.example.xiaozhimed.controller;

import com.example.xiaozhimed.bean.Result;
import com.example.xiaozhimed.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "发送验证码", description = "模拟发送短信验证码，固定返回成功")
    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.isBlank()) {
            return Result.error(400, "手机号不能为空");
        }
        authService.sendCode(phone);
        return Result.success("验证码发送成功", null);
    }

    @Operation(summary = "登录", description = "手机号+验证码登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("code");
        if (phone == null || phone.isBlank()) {
            return Result.error(400, "手机号不能为空");
        }
        if (code == null || code.isBlank()) {
            return Result.error(400, "验证码不能为空");
        }
        return Result.success(authService.login(phone, code));
    }
}
