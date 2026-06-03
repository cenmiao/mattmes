package com.matt.mes.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConcurrentLoginInfo {

    /** 当前登录时间 */
    private LocalDateTime currentLoginTime;

    /** 当前登录IP */
    private String currentLoginIp;
}
