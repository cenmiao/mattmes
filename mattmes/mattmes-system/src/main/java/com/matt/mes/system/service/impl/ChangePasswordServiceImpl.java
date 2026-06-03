package com.matt.mes.system.service.impl;

import com.matt.mes.common.exception.BusinessException;
import com.matt.mes.common.token.TokenStorage;
import com.matt.mes.common.utils.PasswordUtils;
import com.matt.mes.system.dto.ChangePasswordRequest;
import com.matt.mes.system.entity.SysUser;
import com.matt.mes.system.mapper.SysUserMapper;
import com.matt.mes.system.service.ChangePasswordService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChangePasswordServiceImpl implements ChangePasswordService {

    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenStorage tokenStorage;

    public ChangePasswordServiceImpl(SysUserMapper userMapper,
                                     BCryptPasswordEncoder passwordEncoder,
                                     TokenStorage tokenStorage) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenStorage = tokenStorage;
    }

    @Override
    @Transactional
    public String changePassword(ChangePasswordRequest request, Long userId) {
        // 1. 校验两次密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次密码不一致");
        }

        // 2. 校验密码强度
        List<String> strengthErrors = PasswordUtils.validateStrength(request.getNewPassword());
        if (!strengthErrors.isEmpty()) {
            throw new BusinessException(400, String.join("; ", strengthErrors));
        }

        // 3. 获取用户信息
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        // 4. 非首次登录场景需要验证旧密码
        if (!Boolean.TRUE.equals(request.getIsFirstLogin())) {
            if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
                throw new BusinessException(400, "旧密码不能为空");
            }

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new BusinessException(400, "旧密码错误");
            }

            // 5. 校验新密码与旧密码不能相同
            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                throw new BusinessException(400, "新密码不能与旧密码相同");
            }
        }

        // 6. 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setLoginErrorCount(0);

        userMapper.updateById(user);

        // 7. 使Token失效
        tokenStorage.invalidateToken(userId);

        return "密码修改成功，请重新登录";
    }
}