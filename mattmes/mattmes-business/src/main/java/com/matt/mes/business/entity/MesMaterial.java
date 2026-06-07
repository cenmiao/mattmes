package com.matt.mes.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 料号实体
 */
@Data
@TableName("mes_material")
public class MesMaterial {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 料号编码（全局唯一） */
    private String materialCode;

    /** 料号名称 */
    private String materialName;

    /** 所属项目ID */
    private Long projectId;

    /** 绑定的路由ID */
    private Long routeId;

    /** 颜色 */
    private String color;

    /** 尺码 */
    private String size;

    /** 通用规格1 */
    private String spec1;

    /** 通用规格2 */
    private String spec2;

    /** 通用规格3 */
    private String spec3;

    /** 描述 */
    private String description;

    /** 备注 */
    private String remark;

    /** 启用状态：1=启用，0=禁用 */
    private Integer enable;

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