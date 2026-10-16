-- 演示数据（依赖 sql/schema.sql 先执行）
USE `permission_system`;

-- 组织：总公司 - 研发部 - 后端组/前端组，另有财务部
INSERT INTO `sys_org` (`id`, `name`, `parent_id`, `ancestors`, `sort`) VALUES
  (100, '总公司', 0, '0', 1),
  (101, '研发部', 100, '0,100', 2),
  (102, '财务部', 100, '0,100', 3),
  (103, '后端组', 101, '0,100,101', 4),
  (104, '前端组', 101, '0,100,101', 5)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `parent_id` = VALUES(`parent_id`), `ancestors` = VALUES(`ancestors`);

-- 角色：超管(全部数据)、部门主管(本部门及以下)、普通员工(仅本人)
INSERT INTO `sys_role` (`id`, `name`, `code`, `data_scope`, `sort`, `remark`) VALUES
  (1, '超级管理员', 'admin', 1, 1, '拥有全部权限与全部数据'),
  (2, '部门主管', 'dept_manager', 3, 2, '可管理本部门及下级部门数据'),
  (3, '普通员工', 'staff', 4, 3, '只能查看本人数据')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `data_scope` = VALUES(`data_scope`), `remark` = VALUES(`remark`);

-- 权限点：菜单(type=1)、按钮(type=2)
INSERT INTO `sys_permission` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `icon`, `sort`) VALUES
  (1,  '系统管理',     'system',                 1, 0, '/system',     'Setting', 1),
  (10, '用户管理',     'system:user:list',       1, 1, '/system/user', 'User', 1),
  (11, '用户新增',     'system:user:add',        2, 10, NULL, NULL, 1),
  (12, '用户修改',     'system:user:edit',       2, 10, NULL, NULL, 2),
  (13, '用户删除',     'system:user:delete',     2, 10, NULL, NULL, 3),
  (14, '分配角色',     'system:user:assign',     2, 10, NULL, NULL, 4),
  (20, '角色管理',     'system:role:list',       1, 1, '/system/role', 'Avatar', 2),
  (21, '角色新增',     'system:role:add',        2, 20, NULL, NULL, 1),
  (22, '角色修改',     'system:role:edit',       2, 20, NULL, NULL, 2),
  (23, '角色删除',     'system:role:delete',     2, 20, NULL, NULL, 3),
  (24, '分配权限',     'system:role:assign',     2, 20, NULL, NULL, 4),
  (30, '权限管理',     'system:permission:list', 1, 1, '/system/permission', 'Menu', 3),
  (31, '权限新增',     'system:permission:add',  2, 30, NULL, NULL, 1),
  (32, '权限修改',     'system:permission:edit', 2, 30, NULL, NULL, 2),
  (33, '权限删除',     'system:permission:delete', 2, 30, NULL, NULL, 3),
  (40, '组织架构',     'system:org:list',        1, 1, '/system/org', 'OfficeBuilding', 4),
  (41, '组织新增',     'system:org:add',         2, 40, NULL, NULL, 1),
  (42, '组织修改',     'system:org:edit',        2, 40, NULL, NULL, 2),
  (43, '组织删除',     'system:org:delete',      2, 40, NULL, NULL, 3),
  (50, '审计日志',     'system:audit:list',      1, 1, '/system/audit', 'Document', 5)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `code` = VALUES(`code`), `type` = VALUES(`type`), `parent_id` = VALUES(`parent_id`);

-- 用户（密码占位 INIT，应用启动时会自动加密为 123456）
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `org_id`) VALUES
  (1, 'admin',    'INIT', '超级管理员', 100),
  (2, 'zhangsan', 'INIT', '张三·研发主管', 101),
  (3, 'lisi',     'INIT', '李四·后端工程师', 103),
  (4, 'wangwu',   'INIT', '王五·财务专员', 102)
ON DUPLICATE KEY UPDATE `nickname` = VALUES(`nickname`), `org_id` = VALUES(`org_id`);

-- 用户角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1), (2, 2), (3, 3), (4, 3)
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

-- 角色权限：超管=全部；部门主管=用户查看/修改+角色查看；普通员工=仅用户查看
DELETE FROM `sys_role_permission` WHERE `role_id` IN (1, 2, 3);
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, `id` FROM `sys_permission`;
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
  (2, 1), (2, 10), (2, 12), (2, 20),
  (3, 1), (3, 10);
