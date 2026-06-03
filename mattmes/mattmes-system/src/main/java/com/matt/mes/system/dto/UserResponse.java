package com.matt.mes.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    /** 用户ID */
    private Long id;

    /** 工号 */
    private String userNo;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 状态：1=启用，0=禁用 */
    private Integer status;

    /** 禁用原因 */
    private String disableReason;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 角色列表 */
    private List<RoleInfo> roles;

    /**
     * 角色信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {
        private Long id;
        private String roleName;
        private String roleCode;
    }
}
