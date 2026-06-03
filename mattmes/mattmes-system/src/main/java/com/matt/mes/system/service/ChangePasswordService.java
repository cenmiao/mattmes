package com.matt.mes.system.service;

import com.matt.mes.system.dto.ChangePasswordRequest;

public interface ChangePasswordService {

    /**
     * 修改密码
     *
     * @param request 修改请求
     * @param userId  当前用户ID
     * @return 成功消息
     * @throws com.matt.mes.common.exception.BusinessException 修改失败时抛出异常
     */
    String changePassword(ChangePasswordRequest request, Long userId);
}