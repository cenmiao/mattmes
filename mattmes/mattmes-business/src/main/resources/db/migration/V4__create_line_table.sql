-- 线体表创建脚本
-- 说明: 打通从数据库到浏览器的最窄完整路径，线体管理列表页消费此表。
-- 可重复执行（CREATE TABLE IF NOT EXISTS），重复运行不会报错。

CREATE TABLE IF NOT EXISTS `mes_line` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `line_code` VARCHAR(50) NOT NULL COMMENT '线体编码（全局唯一）',
  `line_name` VARCHAR(100) NOT NULL COMMENT '线体名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `enable` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态(1启用/0禁用)',
  `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删除/1已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_line_code` (`line_code`),
  KEY `idx_enable` (`enable`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='线体表';
