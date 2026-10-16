-- 权限系统表结构（MySQL 8.0，字符集 utf8mb4）
-- 说明：表名统一加 sys_ 前缀，避免与 MySQL 8 关键字（role 等）冲突
CREATE DATABASE IF NOT EXISTS `permission_system`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `permission_system`;

-- 1. 组织架构（树形，ancestors 存祖级路径，便于"本部门及以下"查询）
CREATE TABLE IF NOT EXISTS `sys_org`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '组织ID',
    `name`        VARCHAR(64)  NOT NULL COMMENT '组织名称',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父组织ID，0为顶级',
    `ancestors`   VARCHAR(255) NOT NULL DEFAULT '0' COMMENT '祖级ID列表，逗号分隔',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 0停用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_org_parent` (`parent_id`)
) ENGINE = InnoDB COMMENT = '组织架构表';

-- 2. 用户
CREATE TABLE IF NOT EXISTS `sys_user`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(64)  NOT NULL COMMENT '登录账号',
    `password`    VARCHAR(128) NOT NULL COMMENT 'BCrypt 密码',
    `nickname`    VARCHAR(64)  NOT NULL COMMENT '姓名',
    `org_id`      BIGINT       NULL COMMENT '所属组织ID',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 0停用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    KEY `idx_user_org` (`org_id`)
) ENGINE = InnoDB COMMENT = '用户表';

-- 3. 角色（data_scope 决定该角色的数据权限范围）
CREATE TABLE IF NOT EXISTS `sys_role`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `name`        VARCHAR(64)  NOT NULL COMMENT '角色名称',
    `code`        VARCHAR(64)  NOT NULL COMMENT '角色标识',
    `data_scope`  TINYINT      NOT NULL DEFAULT 4 COMMENT '数据范围：1全部 2本部门 3本部门及以下 4仅本人',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 0停用',
    `remark`      VARCHAR(255) NULL COMMENT '备注',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`code`)
) ENGINE = InnoDB COMMENT = '角色表';

-- 4. 权限点（菜单、按钮、接口共用一套编码）
CREATE TABLE IF NOT EXISTS `sys_permission`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `name`        VARCHAR(64)  NOT NULL COMMENT '权限名称',
    `code`        VARCHAR(128) NULL COMMENT '权限编码，如 system:user:delete',
    `type`        TINYINT      NOT NULL COMMENT '类型：1菜单 2按钮 3接口',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父权限ID，0为顶级',
    `path`        VARCHAR(128) NULL COMMENT '前端路由地址',
    `component`   VARCHAR(128) NULL COMMENT '前端组件路径',
    `icon`        VARCHAR(64)  NULL COMMENT '菜单图标',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 0停用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_perm_parent` (`parent_id`),
    KEY `idx_perm_code` (`code`)
) ENGINE = InnoDB COMMENT = '权限点表';

-- 5. 用户-角色关系
CREATE TABLE IF NOT EXISTS `sys_user_role`
(
    `id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE = InnoDB COMMENT = '用户角色关系表';

-- 6. 角色-权限关系
CREATE TABLE IF NOT EXISTS `sys_role_permission`
(
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id`       BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE = InnoDB COMMENT = '角色权限关系表';

-- 7. 操作审计日志
CREATE TABLE IF NOT EXISTS `sys_audit_log`
(
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id`     BIGINT        NULL COMMENT '操作人ID',
    `username`    VARCHAR(64)   NULL COMMENT '操作人账号',
    `module`      VARCHAR(64)   NULL COMMENT '模块',
    `operation`   VARCHAR(128)  NULL COMMENT '操作描述',
    `method`      VARCHAR(255)  NULL COMMENT '请求方法（类名.方法名）',
    `request_uri` VARCHAR(255)  NULL COMMENT '请求地址',
    `request_method` VARCHAR(16) NULL COMMENT 'HTTP 方法',
    `params`      VARCHAR(2000) NULL COMMENT '请求参数（JSON，超长截断）',
    `ip`          VARCHAR(64)   NULL COMMENT '客户端IP',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '结果：1成功 0失败',
    `error_msg`   VARCHAR(500)  NULL COMMENT '异常信息',
    `cost_time`   BIGINT        NULL COMMENT '耗时（毫秒）',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_audit_user` (`user_id`),
    KEY `idx_audit_time` (`create_time`)
) ENGINE = InnoDB COMMENT = '操作审计日志表';
