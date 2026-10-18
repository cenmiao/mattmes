-- 线体模块权限初始化
-- 说明: 建立 `line` 模块权限与 `line:read`、`line:add`、`line:edit`、`line:delete`、`line:export` 五个权限点。
-- 后续 ticket 只消费不新建。脚本可重复执行，重复运行不产生重复记录。
-- 与料号模块(permission seed in V3)格式保持一致。

-- ------------------------------------------------------------
-- 1. 线体模块父权限
-- ------------------------------------------------------------
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '线体管理', 'line', 1, NULL, 80, '线体管理模块', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'line');

-- ------------------------------------------------------------
-- 2. 线体模块按钮权限
-- ------------------------------------------------------------
-- 线体查询
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '线体查询', 'line:read', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='line') t), 5, '查看线体列表', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'line:read');

-- 线体新增
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '线体新增', 'line:add', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='line') t), 10, '新增线体', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'line:add');

-- 线体编辑
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '线体编辑', 'line:edit', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='line') t), 20, '编辑线体', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'line:edit');

-- 线体导出
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '线体导出', 'line:export', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='line') t), 30, '导出线体数据为CSV', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'line:export');

-- 线体删除
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `sort_order`, `description`, `created_by`, `create_time`)
SELECT '线体删除', 'line:delete', 2, (SELECT id FROM (SELECT id FROM `sys_permission` WHERE `permission_code`='line') t), 40, '删除线体', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'line:delete');

-- ------------------------------------------------------------
-- 3. SUPER_ADMIN 角色授予线体模块所有权限
--    注意: 代码层面超管已跳过权限校验，此处授全是为了让
--          权限管理页面展示与实际保持一致
-- ------------------------------------------------------------
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `created_by`, `create_time`, `updated_by`, `update_time`)
SELECT r.id, p.id, 'system', NOW(), 'system', NOW()
FROM `sys_role` r, `sys_permission` p
WHERE r.role_code = 'SUPER_ADMIN'
  AND p.permission_code IN ('line', 'line:read', 'line:add', 'line:edit', 'line:delete', 'line:export')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
