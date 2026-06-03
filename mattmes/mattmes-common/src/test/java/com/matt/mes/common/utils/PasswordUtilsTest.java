package com.matt.mes.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {

    @Test
    @DisplayName("密码强度校验 - 长度不足8位")
    void validateStrength_tooShort() {
        // Given: 长度不足8位的密码
        String password = "Abc123";

        // When: 校验密码强度
        List<String> errors = PasswordUtils.validateStrength(password);

        // Then: 返回长度不足的错误
        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("密码长度至少8位"));
    }

    @Test
    @DisplayName("密码强度校验 - 缺少字母")
    void validateStrength_missingLetter() {
        // Given: 只有数字的密码
        String password = "12345678";

        // When: 校验密码强度
        List<String> errors = PasswordUtils.validateStrength(password);

        // Then: 返回缺少字母的错误
        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("密码必须包含至少1个字母"));
    }

    @Test
    @DisplayName("密码强度校验 - 缺少数字")
    void validateStrength_missingNumber() {
        // Given: 只有字母的密码
        String password = "Abcdefgh";

        // When: 校验密码强度
        List<String> errors = PasswordUtils.validateStrength(password);

        // Then: 返回缺少数字的错误
        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("密码必须包含至少1个数字"));
    }

    @Test
    @DisplayName("密码强度校验 - 满足所有要求")
    void validateStrength_valid() {
        // Given: 符合要求的密码
        String password = "Admin@123";

        // When: 校验密码强度
        List<String> errors = PasswordUtils.validateStrength(password);

        // Then: 返回空列表
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("密码强度校验 - 多个错误同时存在")
    void validateStrength_multipleErrors() {
        // Given: 既短又缺少数字的密码
        String password = "Abc";

        // When: 校验密码强度
        List<String> errors = PasswordUtils.validateStrength(password);

        // Then: 返回多个错误
        assertEquals(2, errors.size());
        assertTrue(errors.contains("密码长度至少8位"));
        assertTrue(errors.contains("密码必须包含至少1个数字"));
    }
}
