package com.matt.mes.common.utils;

import java.util.ArrayList;
import java.util.List;

public class PasswordUtils {

    /**
     * 校验密码强度
     *
     * @param password 密码
     * @return 不满足的原因列表，空列表表示通过
     */
    public static List<String> validateStrength(String password) {
        List<String> errors = new ArrayList<>();

        // 长度校验
        if (password == null || password.length() < 8) {
            errors.add("密码长度至少8位");
        }

        // 字母校验
        if (password != null && !password.matches(".*[a-zA-Z].*")) {
            errors.add("密码必须包含至少1个字母");
        }

        // 数字校验
        if (password != null && !password.matches(".*[0-9].*")) {
            errors.add("密码必须包含至少1个数字");
        }

        return errors;
    }
}