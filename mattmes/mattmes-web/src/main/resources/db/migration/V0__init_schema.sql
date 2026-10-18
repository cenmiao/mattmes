-- ============================================================
-- Matt MES 数据库初始化脚本
-- 创建时间: 2026-10-17
-- 说明: 本项目未引入 Flyway/Liquibase，此脚本需手工执行
--       执行方式: mysql -uroot -p123456 < V0__init_schema.sql
-- 字符集: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `mattmes`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `mattmes`;

-- ------------------------------------------------------------
-- 1. 用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_no` VARCHAR(50) NOT NULL COMMENT '工号（唯一）',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
  `disable_reason` VARCHAR(50) DEFAULT NULL COMMENT '禁用原因：ADMIN_MANUAL/AUTO_INACTIVE',
  `password_update_time` DATETIME DEFAULT NULL COMMENT '密码最后修改时间',
  `login_error_count` INT NOT NULL DEFAULT 0 COMMENT '连续登录错误次数',
  `lock_time` DATETIME DEFAULT NULL COMMENT '锁定截止时间',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `current_token` VARCHAR(2000) DEFAULT NULL COMMENT '当前登录Token（单终端控制）',
  `token_expire_time` DATETIME DEFAULT NULL COMMENT 'Token过期时间',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_no` (`user_no`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ------------------------------------------------------------
-- 2. 角色表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码（唯一）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '角色描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ------------------------------------------------------------
-- 3. 权限表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码（如 user:add）',
  `permission_type` TINYINT NOT NULL DEFAULT 2 COMMENT '权限类型：1=模块，2=按钮',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父权限ID（NULL表示顶级）',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '权限描述',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_permission_type` (`permission_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ------------------------------------------------------------
-- 4. 用户角色关联表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ------------------------------------------------------------
-- 5. 角色权限关联表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ------------------------------------------------------------
-- 6. 永久Token表（CONTEXT.md 中定义，当前代码尚未使用）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_permanent_token` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
  `token` VARCHAR(500) NOT NULL COMMENT 'Token值',
  `description` VARCHAR(500) DEFAULT NULL COMMENT 'Token用途说明',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token` (`token`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='永久Token表';

-- ------------------------------------------------------------
-- 7. 登录日志表（CONTEXT.md 中定义，当前代码尚未使用）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_no` VARCHAR(50) NOT NULL COMMENT '工号',
  `login_time` DATETIME DEFAULT NULL COMMENT '登录时间',
  `login_ip` VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
  `device_info` VARCHAR(500) DEFAULT NULL COMMENT '设备信息',
  `login_result` TINYINT NOT NULL DEFAULT 1 COMMENT '登录结果：1=成功，0=失败',
  `fail_reason` VARCHAR(200) DEFAULT NULL COMMENT '失败原因',
  `logout_time` DATETIME DEFAULT NULL COMMENT '退出时间',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_no` (`user_no`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- ------------------------------------------------------------
-- 8. 工序表（与 V1__create_process_table.sql 保持一致）
-- ------------------------------------------------------------
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
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_process_type` (`process_type`),
  KEY `idx_enable` (`enable`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工序表';

-- ------------------------------------------------------------
-- 9. 项目表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mes_project` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` VARCHAR(50) NOT NULL COMMENT '项目编码',
  `name` VARCHAR(100) NOT NULL COMMENT '项目名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '项目描述',
  `enable` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态(1启用/0禁用)',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删除/1已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_enable` (`enable`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- ------------------------------------------------------------
-- 10. 料号表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mes_material` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `material_code` VARCHAR(50) NOT NULL COMMENT '料号编码（全局唯一）',
  `material_name` VARCHAR(100) NOT NULL COMMENT '料号名称',
  `project_id` BIGINT NOT NULL COMMENT '所属项目ID',
  `route_id` BIGINT DEFAULT NULL COMMENT '绑定的路由ID',
  `color` VARCHAR(50) DEFAULT NULL COMMENT '颜色',
  `size` VARCHAR(50) DEFAULT NULL COMMENT '尺码',
  `spec1` VARCHAR(100) DEFAULT NULL COMMENT '通用规格1',
  `spec2` VARCHAR(100) DEFAULT NULL COMMENT '通用规格2',
  `spec3` VARCHAR(100) DEFAULT NULL COMMENT '通用规格3',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `enable` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态(1启用/0禁用)',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删除/1已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_code` (`material_code`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_route_id` (`route_id`),
  KEY `idx_enable` (`enable`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='料号表';
