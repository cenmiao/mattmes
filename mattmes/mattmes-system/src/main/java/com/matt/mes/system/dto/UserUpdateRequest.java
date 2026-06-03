package com.matt.mes.system.dto;

import lombok.Data;

import java.util.List;

/**
 * 编辑用户请求DTO
 */
@Data
public class UserUpdateRequest {

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色ID列表（可选，传入时不为null则更新角色） */
    private List<Long> roleIds;
}
