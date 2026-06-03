package com.matt.mes.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "工号不能为空")
    private String userNo;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 是否强制登录（踢掉其他设备） */
    private Boolean forceLogin = false;
}
