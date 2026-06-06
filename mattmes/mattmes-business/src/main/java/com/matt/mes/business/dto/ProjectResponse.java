package com.matt.mes.business.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 项目响应对象
 */
@Data
public class ProjectResponse {
    /** 项目ID */
    private Long id;
    /** 项目编码 */
    private String code;
    /** 项目名称 */
    private String name;
    /** 项目描述 */
    private String description;
    /** 启用状态 */
    private Integer enable;
    /** 备注 */
    private String remark;
    /** 创建人 */
    private String createdBy;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新人 */
    private String updatedBy;
    /** 更新时间 */
    private LocalDateTime updateTime;
}