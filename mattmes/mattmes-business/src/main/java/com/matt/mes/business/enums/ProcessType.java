package com.matt.mes.business.enums;

/**
 * 工序类型枚举
 */
public enum ProcessType {

    /** 检测工序 */
    INSPECTION("INSPECTION", "检测"),

    /** 组装工序 */
    ASSEMBLY("ASSEMBLY", "组装"),

    /** 包装工序 */
    PACKAGING("PACKAGING", "包装"),

    /** 其他工序 */
    OTHER("OTHER", "其他");

    private final String code;
    private final String name;

    ProcessType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
