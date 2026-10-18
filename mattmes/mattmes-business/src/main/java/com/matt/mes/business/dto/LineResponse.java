package com.matt.mes.business.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 线体响应对象
 */
@Data
public class LineResponse {
    /** 线体ID */
    private Long id;
    /** 线体编码 */
    private String code;
    /** 线体名称 */
    private String name;
    /** 描述 */
    private String description;
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
