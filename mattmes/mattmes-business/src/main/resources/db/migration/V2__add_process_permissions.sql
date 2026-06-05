-- 添加工序导出权限
-- 说明：前端 ProcessList.vue 使用 v-permission="'process:export'" 控制导出按钮

INSERT INTO sys_permission (permission_name, permission_code, permission_type, parent_id, sort_order, description, created_by, create_time)
SELECT '工序导出', 'process:export', 2,
    COALESCE((SELECT id FROM (SELECT id FROM sys_permission WHERE permission_code = 'process') t), 0),
    30,
    '导出工序数据为CSV文件',
    'admin',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'process:export');

-- 同时确保工序模块父权限存在（如果不存在则创建）
INSERT INTO sys_permission (permission_name, permission_code, permission_type, parent_id, sort_order, description, created_by, create_time)
SELECT '工序管理', 'process', 1, 0, 10, '工序管理模块权限', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'process');

-- 确保工序新增和编辑权限存在
INSERT INTO sys_permission (permission_name, permission_code, permission_type, parent_id, sort_order, description, created_by, create_time)
SELECT '工序新增', 'process:add', 2,
    COALESCE((SELECT id FROM (SELECT id FROM sys_permission WHERE permission_code = 'process') t), 0),
    10,
    '新增工序',
    'admin',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'process:add');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, parent_id, sort_order, description, created_by, create_time)
SELECT '工序编辑', 'process:edit', 2,
    COALESCE((SELECT id FROM (SELECT id FROM sys_permission WHERE permission_code = 'process') t), 0),
    20,
    '编辑工序',
    'admin',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'process:edit');