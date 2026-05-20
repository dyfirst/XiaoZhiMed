package com.example.xiaozhimed.controller;

import com.example.xiaozhimed.bean.ChatSessionDoc;
import com.example.xiaozhimed.bean.Result;
import com.example.xiaozhimed.interceptor.AuthInterceptor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "聊天会话管理")
@RestController
@RequestMapping("/chat-sessions")
public class ChatSessionController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Operation(summary = "加载当前用户的所有会话")
    @GetMapping
    public Result<List<ChatSessionDoc>> loadSessions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        Query query = new Query(Criteria.where("userId").is(userId));
        List<ChatSessionDoc> sessions = mongoTemplate.find(query, ChatSessionDoc.class);
        return Result.success(sessions);
    }

    @Operation(summary = "更新会话标题")
    @PutMapping("/{sessionId}/title")
    public Result<Void> updateTitle(@PathVariable String sessionId, @RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        String title = body.get("title");
        Query query = new Query(Criteria.where("userId").is(userId).and("sessionId").is(sessionId));
        Update update = new Update().set("title", title);
        mongoTemplate.updateFirst(query, update, ChatSessionDoc.class);
        return Result.success();
    }

    @Operation(summary = "删除一个会话")
    @DeleteMapping("/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        Query query = new Query(Criteria.where("userId").is(userId).and("sessionId").is(sessionId));
        mongoTemplate.remove(query, ChatSessionDoc.class);
        return Result.success();
    }
}
