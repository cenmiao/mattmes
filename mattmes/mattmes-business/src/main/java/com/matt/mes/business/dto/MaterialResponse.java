package com.matt.mes.business.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 料号响应对象
 */
@Data
public class MaterialResponse {
    /** 料号ID */
    private Long id;
    /** 料号编码 */
    private String code;
    /** 料号名称 */
    private String name;
    /** 所属项目ID */
    private Long projectId;
    /** 所属项目名称 */
    private String projectName;
    /** 绑定的路由ID */
    private Long routeId;
    /** 绑定的路由名称 */
    private String routeName;
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
    /** 启用状态 */
    private Integer enable;
    /** 创建人 */
    private String createdBy;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新人 */
    private String updatedBy;
    /** 更新时间 */
    private LocalDateTime updateTime;
}