package com.matt.mes.system.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建用户请求DTO
 */
@Data
public class UserCreateRequest {

    /** 工号 */
    private String userNo;

    /** 姓名 */
    private String name;

    /** 初始密码（可选，不填则使用默认密码） */
    private String password;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色ID列表 */
    private List<Long> roleIds;
}
