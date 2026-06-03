package com.matt.mes.system.service;

import com.matt.mes.system.dto.ConcurrentLoginInfo;
import com.matt.mes.system.dto.LoginRequest;
import com.matt.mes.system.dto.LoginResponse;

public interface LoginService {

    /**
     * 登录认证
     *
     * @param request 登录请求
     * @param loginIp 登录IP
     * @return 登录响应（成功时返回Token和用户信息）
     * @throws com.matt.mes.common.exception.BusinessException 登录失败时抛出异常
     */
    LoginResponse login(LoginRequest request, String loginIp);

    /**
     * 检查并发登录冲突
     *
     * @param request 登录请求
     * @return 如果存在并发登录冲突，返回当前登录信息；否则返回null
     */
    ConcurrentLoginInfo checkConcurrentLogin(LoginRequest request);
}