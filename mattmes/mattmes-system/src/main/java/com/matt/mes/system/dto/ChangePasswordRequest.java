package com.matt.mes.system.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {

    /** 旧密码（非首次登录场景必填） */
    private String oldPassword;

    /** 新密码 */
    private String newPassword;

    /** 确认密码 */
    private String confirmPassword;

    /** 是否首次登录 */
    private Boolean isFirstLogin;
}
