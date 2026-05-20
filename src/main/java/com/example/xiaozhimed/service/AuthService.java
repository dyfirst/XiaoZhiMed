package com.example.xiaozhimed.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xiaozhimed.entity.User;
import com.example.xiaozhimed.exception.BizException;
import com.example.xiaozhimed.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class AuthService {

    private static final String FIXED_CODE = "123456";
    private static final long EXPIRE_MS = 24 * 3600 * 1000; // 24小时

    @Value("${jwt.secret:xiaozhiMedJwtSecretKey2024!MustBe32BytesLong!!}")
    private String jwtSecret;

    private final UserMapper userMapper;

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public void sendCode(String phone) {
        log.info("发送验证码: phone={}, code={}", phone, FIXED_CODE);
    }

    public Map<String, Object> login(String phone, String code) {
        if (!FIXED_CODE.equals(code)) {
            throw new BizException(400, "验证码错误");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setName("用户" + phone.substring(phone.length() - 4));
            userMapper.insert(user);
            log.info("自动注册新用户: phone={}, userId={}", phone, user.getId());
        }

        String token = generateToken(user.getId(), phone);

        return Map.of(
                "token", token,
                "userId", user.getId(),
                "name", user.getName(),
                "phone", user.getPhone()
        );
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private String generateToken(Long userId, String phone) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("phone", phone)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_MS))
                .signWith(getSecretKey())
                .compact();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
