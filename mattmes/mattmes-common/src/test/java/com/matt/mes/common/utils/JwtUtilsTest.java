package com.matt.mes.common.utils;

import org.junit.jupiter.api.Test;

class JwtUtilsTest {

    @Test
    void testGenerateAndParseToken() {
        String token = JwtUtils.generateToken(1L, "admin");
        System.out.println("Token: " + token);

        Long userId = JwtUtils.getUserId(token);
        System.out.println("UserId: " + userId);

        String userNo = JwtUtils.getUserNo(token);
        System.out.println("UserNo: " + userNo);

        boolean expired = JwtUtils.isExpired(token);
        System.out.println("Expired: " + expired);

        assert userId == 1L;
        assert "admin".equals(userNo);
        assert !expired;
    }

    @Test
    void testLongTermToken() {
        // 生成365天的长期token
        String longTermToken = JwtUtils.generateTokenWithDays(1L, "admin", 365);
        System.out.println("Long Term Token: " + longTermToken);

        boolean isLongTerm = JwtUtils.isLongTermToken(longTermToken);
        System.out.println("Is Long Term: " + isLongTerm);

        // 测试我们生成的永久token
        String permanentToken = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VyTm8iOiJhZG1pbiIsInVzZXJJZCI6MSwiaWF0IjoxNzgwNDg2OTEyLCJleHAiOjE4MTIwMjI5MTJ9.be7sM94QmpqKrkr3iYWMRkROzaKyb-LGZNF3SW93VPzSzjEEjJy06zbOCeOjsTGK";
        boolean isPermanent = JwtUtils.isLongTermToken(permanentToken);
        System.out.println("Is Permanent Token: " + isPermanent);

        assert isLongTerm;
        assert isPermanent;
    }
}
