-- 工序表创建脚本
-- 创建时间: 2026-06-04

CREATE TABLE IF NOT EXISTS `mes_process` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` VARCHAR(50) NOT NULL COMMENT '工序编码',
  `name` VARCHAR(100) NOT NULL COMMENT '工序名称',
  `process_type` VARCHAR(20) NOT NULL COMMENT '工序类型(INSPECTION/ASSEMBLY/PACKAGING/OTHER)',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '工序描述',
  `enable` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态(1启用/0禁用)',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删除/1已删除)',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_code` (`code`),
  INDEX `idx_process_type` (`process_type`),
  INDEX `idx_enable` (`enable`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工序表';
