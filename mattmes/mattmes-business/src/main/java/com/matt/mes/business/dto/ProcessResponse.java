package com.matt.mes.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工序响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessResponse {

    /** 工序ID */
    private Long id;

    /** 工序编码 */
    private String code;

    /** 工序名称 */
    private String name;

    /** 工序类型 */
    private String processType;

    /** 工序描述 */
    private String description;

    /** 启用状态:1=启用,0=禁用 */
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
