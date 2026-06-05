package com.matt.mes.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工序实体
 */
@Data
@TableName("mes_process")
public class MesProcess {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工序编码（唯一） */
    private String code;

    /** 工序名称 */
    private String name;

    /** 工序类型(INSPECTION/ASSEMBLY/PACKAGING/OTHER) */
    private String processType;

    /** 工序描述 */
    private String description;

    /** 启用状态：1=启用，0=禁用 */
    private Integer enable;

    /** 备注 */
    private String remark;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.UPDATE)
    private String updatedBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;
}
