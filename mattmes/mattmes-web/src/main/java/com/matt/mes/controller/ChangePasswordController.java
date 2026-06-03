package com.matt.mes.controller;

import com.matt.mes.common.result.Result;
import com.matt.mes.system.dto.ChangePasswordRequest;
import com.matt.mes.system.service.ChangePasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChangePasswordController {

    private final ChangePasswordService changePasswordService;

    @PostMapping("/change-password")
    public Result<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        // TODO: 从Token中获取当前用户ID
        Long userId = getCurrentUserId();
        String message = changePasswordService.changePassword(request, userId);
        return Result.success(message);
    }

    private Long getCurrentUserId() {
        // TODO: 实现从安全上下文获取用户ID
        // 暂时返回1L用于测试
        return 1L;
    }
}
