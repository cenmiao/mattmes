package com.matt.mes.system.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.matt.mes.common.exception.BusinessException;
import com.matt.mes.common.utils.JwtUtils;
import com.matt.mes.system.entity.SysUser;
import com.matt.mes.system.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final SysUserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从Header获取Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new BusinessException(401, "请先登录");
        }

        // Bearer Token格式
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 解析Token
            Long userId = JwtUtils.getUserId(token);
            String userNo = JwtUtils.getUserNo(token);

            // 检查Token是否过期
            if (JwtUtils.isExpired(token)) {
                throw new BusinessException(401, "登录已过期，请重新登录");
            }

            // 查询用户当前Token
            SysUser user = userMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getId, userId)
                            .select(SysUser::getCurrentToken, SysUser::getTokenExpireTime, SysUser::getStatus)
            );

            if (user == null) {
                throw new BusinessException(401, "用户不存在");
            }

            // 检查账号是否被禁用
            if (user.getStatus() == null || user.getStatus() == 0) {
                throw new BusinessException(401, "账号已被禁用");
            }

            // 检查Token是否匹配（被强制登出检测）
            // 注意：永久测试token跳过单点登录验证
            if (!JwtUtils.isLongTermToken(token) && !token.equals(user.getCurrentToken())) {
                throw new BusinessException(401, "账号已在其他设备登录，请重新登录");
            }

            // 检查Token过期时间（永久token跳过此检查）
            if (!JwtUtils.isLongTermToken(token) && user.getTokenExpireTime() != null && user.getTokenExpireTime().isBefore(LocalDateTime.now())) {
                throw new BusinessException(401, "登录已过期，请重新登录");
            }

            // 将用户信息存入Request
            request.setAttribute("userId", userId);
            request.setAttribute("userNo", userNo);

            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(401, "Token无效");
        }
    }
}