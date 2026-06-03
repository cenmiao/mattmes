package com.matt.mes.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** JWT Token */
    private String token;

    /** 用户ID */
    private Long userId;

    /** 工号 */
    private String userNo;

    /** 姓名 */
    private String name;

    /** 是否需要修改密码 */
    private Boolean needChangePassword;

    /** 权限列表 */
    private List<String> permissions;

    /** 角色列表 */
    private List<RoleInfo> roles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {
        private Long roleId;
        private String roleCode;
        private String roleName;
    }
}
