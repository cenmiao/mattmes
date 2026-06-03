package com.matt.mes.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工号（唯一） */
    private String userNo;

    /** 密码（BCrypt加密） */
    private String password;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 状态：1=启用，0=禁用 */
    private Integer status;

    /** 禁用原因：ADMIN_MANUAL, AUTO_INACTIVE */
    private String disableReason;

    /** 密码最后修改时间 */
    private LocalDateTime passwordUpdateTime;

    /** 连续登录错误次数 */
    private Integer loginErrorCount;

    /** 锁定截止时间 */
    private LocalDateTime lockTime;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 当前登录Token */
    private String currentToken;

    /** Token过期时间 */
    private LocalDateTime tokenExpireTime;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新人 */
    @TableField(value = "updated_by", fill = FieldFill.UPDATE)
    private String updatedBy;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;
}
