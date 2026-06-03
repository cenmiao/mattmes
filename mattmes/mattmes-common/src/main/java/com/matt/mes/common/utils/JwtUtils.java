package com.matt.mes.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    private static final String SECRET_KEY = "mattmes-jwt-secret-key-must-be-at-least-256-bits-long";
    private static final long EXPIRATION_HOURS = 6;
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    // 测试用永久 token，跳过单点登录验证
    private static final String PERMANENT_TEST_TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VyTm8iOiJhZG1pbiIsInVzZXJJZCI6MSwiaWF0IjoxNzgwNDg2OTEyLCJleHAiOjE4MTIwMjI5MTJ9.be7sM94QmpqKrkr3iYWMRkROzaKyb-LGZNF3SW93VPzSzjEEjJy06zbOCeOjsTGK";

    public static String generateToken(Long userId, String userNo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userNo", userNo);

        Date now = new Date();
        Date expiration = Date.from(
                LocalDateTime.now()
                        .plusHours(EXPIRATION_HOURS)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(KEY)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return claims.get("userId", Long.class);
    }

    public static String getUserNo(String token) {
        Claims claims = parseToken(token);
        return claims.get("userNo", String.class);
    }

    public static boolean isExpired(String token) {
        // 永久测试 token 不检查过期
        if (PERMANENT_TEST_TOKEN.equals(token)) {
            return false;
        }
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 判断是否为长期 token
     * 直接比对硬编码的永久测试 token，跳过单点登录验证
     */
    public static boolean isLongTermToken(String token) {
        return PERMANENT_TEST_TOKEN.equals(token);
    }

    public static LocalDateTime getExpirationTime() {
        return LocalDateTime.now().plusHours(EXPIRATION_HOURS);
    }

    /**
     * 生成指定天数有效期的 token（用于测试）
     */
    public static String generateTokenWithDays(Long userId, String userNo, long days) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userNo", userNo);

        Date now = new Date();
        Date expiration = Date.from(
                LocalDateTime.now()
                        .plusDays(days)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(KEY)
                .compact();
    }

    public static void main(String[] args) {
        if (args.length >= 3) {
            Long userId = Long.parseLong(args[0]);
            String userNo = args[1];
            long days = Long.parseLong(args[2]);
            System.out.println(generateTokenWithDays(userId, userNo, days));
        } else {
            // 默认生成 admin 用户，有效期 365 天
            System.out.println(generateTokenWithDays(1L, "admin", 365));
        }
    }
}
