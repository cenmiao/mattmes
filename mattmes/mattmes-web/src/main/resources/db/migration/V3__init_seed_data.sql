-- ============================================================
-- Matt MES 初始化种子数据
-- 创建时间: 2026-10-17
-- 说明: 需在 V0__init_schema.sql 之后执行
-- 默认账号: admin / Admin@123
-- ============================================================

USE `mattmes`;

-- ------------------------------------------------------------
-- 1. 超级管理员用户
--    密码 Admin@123 的 BCrypt 密文
--    首次登录因 password_update_time 为 NULL，会强制修改密码
-- ------------------------------------------------------------
INSERT INTO `sys_user`
  (`user_no`, `password`, `name`, `phone`, `email`, `status`,
   `password_update_time`, `login_error_count`, `created_by`, `create_time`, `updated_by`, `update_time`, `deleted`)
SELECT 'admin',
       '$2a$10$CqergpVlfM6INwONWWBlWexKTVETiF9MvbEnp24ifRoXngpA/XIui',
       '超级管理员',
       NULL,
       NULL,
       1,
       NULL,
       0,
       'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `user_no` = 'admin');

-- ------------------------------------------------------------
-- 2. 超级管理员角色
-- ------------------------------------------------------------
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`, `status`, `created_by`, `create_time`, `updated_by`, `update_time`)
SELECT '超级管理员', 'SUPER_ADMIN', '系统内置超级管理员，拥有所有权限', 1, 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_code` = 'SUPER_ADMIN');

-- ------------------------------------------------------------
-- 3. 权限数据
--    模块级(permission_type=1) -> 按钮级(permission_type=2)
--    parent_id 为 NULL 表示顶级模块
-- ------------------------------------------------------------

-- 3.1 顶级模块
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '用户管理', 'user', 1, NULL, 10, '用户管理模块', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'user');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '角色管理', 'role', 1, NULL, 20, '角色管理模块', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'role');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '权限管理', 'permission', 1, NULL, 30, '权限管理模块', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'permission');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '登录日志', 'login-log', 1, NULL, 40, '登录日志模块', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'login-log');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '工序管理', 'process', 1, NULL, 50, '工序管理模块', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'process');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '项目管理', 'project', 1, NULL, 60, '项目管理模块', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'project');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '料号管理', 'material', 1, NULL, 70, '料号管理模块', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'material');

-- 3.2 用户模块按钮
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '用户查询', 'user:read', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='user') t), 10, '查看用户列表', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'user:read');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '用户新增', 'user:add', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='user') t), 20, '新增用户', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'user:add');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '用户编辑', 'user:edit', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='user') t), 30, '编辑用户', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'user:edit');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '用户删除', 'user:delete', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='user') t), 40, '删除用户', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'user:delete');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '用户禁用', 'user:disable', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='user') t), 50, '启用/禁用用户', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'user:disable');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '重置密码', 'user:reset-password', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='user') t), 60, '重置用户密码', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'user:reset-password');

-- 3.3 角色模块按钮
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '角色查询', 'role:read', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='role') t), 10, '查看角色列表', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'role:read');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '角色新增', 'role:add', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='role') t), 20, '新增角色', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'role:add');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '角色编辑', 'role:edit', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='role') t), 30, '编辑角色', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'role:edit');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '角色删除', 'role:delete', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='role') t), 40, '删除角色', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'role:delete');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '分配权限', 'role:assign-permission', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='role') t), 50, '为角色分配权限', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'role:assign-permission');

-- 3.4 权限模块按钮
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '权限查询', 'permission:read', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='permission') t), 10, '查看权限列表', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'permission:read');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '权限新增', 'permission:add', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='permission') t), 20, '新增权限', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'permission:add');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '权限编辑', 'permission:edit', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='permission') t), 30, '编辑权限', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'permission:edit');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '权限删除', 'permission:delete', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='permission') t), 40, '删除权限', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'permission:delete');

-- 3.5 登录日志模块按钮
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '日志查询', 'login-log:read', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='login-log') t), 10, '查询登录日志', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'login-log:read');

-- 3.6 工序模块按钮
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '工序查询', 'process:read', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='process') t), 5, '查看工序列表', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'process:read');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '工序新增', 'process:add', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='process') t), 10, '新增工序', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'process:add');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '工序编辑', 'process:edit', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='process') t), 20, '编辑工序', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'process:edit');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '工序导出', 'process:export', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='process') t), 30, '导出工序数据为CSV', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'process:export');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '工序删除', 'process:delete', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='process') t), 40, '删除工序', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'process:delete');

-- 3.7 项目模块按钮
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '项目查询', 'project:read', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='project') t), 5, '查看项目列表', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'project:read');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '项目新增', 'project:add', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='project') t), 10, '新增项目', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'project:add');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '项目编辑', 'project:edit', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='project') t), 20, '编辑项目', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'project:edit');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '项目导出', 'project:export', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='project') t), 30, '导出项目数据为CSV', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'project:export');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '项目删除', 'project:delete', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='project') t), 40, '删除项目', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'project:delete');

-- 3.8 料号模块按钮
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '料号查询', 'material:read', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='material') t), 5, '查看料号列表', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'material:read');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '料号新增', 'material:add', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='material') t), 10, '新增料号', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'material:add');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '料号编辑', 'material:edit', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='material') t), 20, '编辑料号', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'material:edit');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '料号导出', 'material:export', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='material') t), 30, '导出料号数据为CSV', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'material:export');

INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '料号删除', 'material:delete', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='material') t), 40, '删除料号', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'material:delete');

-- ------------------------------------------------------------
-- 4. 绑定 admin 用户到 SUPER_ADMIN 角色
-- ------------------------------------------------------------
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `created_by`, `create_time`, `updated_by`, `update_time`)
SELECT u.id, r.id, 'system', NOW(), 'system', NOW()
FROM `sys_user` u, `sys_role` r
WHERE u.user_no = 'admin' AND r.role_code = 'SUPER_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_user_role` ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- ------------------------------------------------------------
-- 5. SUPER_ADMIN 角色授予所有权限
--    注意: 代码层面超管已跳过权限校验，此处授全是为了让
--          权限管理页面展示与实际保持一致
-- ------------------------------------------------------------
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `created_by`, `create_time`, `updated_by`, `update_time`)
SELECT r.id, p.id, 'system', NOW(), 'system', NOW()
FROM `sys_role` r, `sys_permission` p
WHERE r.role_code = 'SUPER_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
