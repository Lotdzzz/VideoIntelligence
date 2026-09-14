-- ----------------------------
-- 用户信息表
-- ----------------------------
drop table if exists sys_user;
create table sys_user
(
    user_id         bigint(20)      not null auto_increment    comment '用户ID',
    user_name       varchar(30) not null comment '用户账号',
    nick_name       varchar(30) not null comment '用户昵称',
    email           varchar(50)  default '' comment '用户邮箱',
    avatar          varchar(100) default '' comment '头像地址',
    password        varchar(100) default '' comment '密码',
    status          char(1)      default '0' comment '账号状态（0正常 1停用）',
    del_flag        char(1)      default '0' comment '删除标志（0代表存在 2代表删除）',
    login_ip        varchar(128) default '' comment '最后登录IP',
    login_date      datetime comment '最后登录时间',
    pwd_update_date datetime comment '密码最后更新时间',
    create_by       varchar(64)  default '' comment '创建者',
    create_time     datetime comment '创建时间',
    update_by       varchar(64)  default '' comment '更新者',
    update_time     datetime comment '更新时间',
    remark          varchar(500) default null comment '备注',
    primary key (user_id)
) engine=innodb auto_increment=100 comment = '用户信息表';

-- 插入用户数据
INSERT INTO `sys_user` (`user_id`, `user_name`, `nick_name`, `email`, `avatar`, `password`, `status`, `del_flag`,
                        `login_ip`, `login_date`, `pwd_update_date`, `create_by`, `create_time`, `update_by`,
                        `update_time`, `remark`)
VALUES (1, 'admin', 'Administrator', 'admin@example.com', NULL,
        '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '1', '0', NULL, NULL, NULL, 'system', NOW(),
        NULL, NOW(), 'Admin user'),
       (2, 'user', 'Regular User', 'user@example.com', NULL,
        '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '1', '0', NULL, NULL, NULL, 'system', NOW(),
        NULL, NOW(), 'Regular user');

-- ----------------------------
-- 角色信息表
-- ----------------------------
drop table if exists sys_role;
create table sys_role
(
    role_id             bigint(20)      not null auto_increment    comment '角色ID',
    role_name           varchar(30)  not null comment '角色名称',
    role_key            varchar(100) not null comment '角色权限字符串',
    role_sort           int(4)          not null                   comment '显示顺序',
    data_scope          char(1)      default '1' comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
    menu_check_strictly tinyint(1)      default 1                  comment '菜单树选择项是否关联显示',
    dept_check_strictly tinyint(1)      default 1                  comment '部门树选择项是否关联显示',
    status              char(1)      not null comment '角色状态（0正常 1停用）',
    del_flag            char(1)      default '0' comment '删除标志（0代表存在 2代表删除）',
    create_by           varchar(64)  default '' comment '创建者',
    create_time         datetime comment '创建时间',
    update_by           varchar(64)  default '' comment '更新者',
    update_time         datetime comment '更新时间',
    remark              varchar(500) default null comment '备注',
    primary key (role_id)
) engine=innodb auto_increment=100 comment = '角色信息表';

-- ----------------------------
-- 菜单权限表
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu
(
    menu_id     bigint(20)      not null auto_increment    comment '菜单ID',
    menu_name   varchar(50) not null comment '菜单名称',
    parent_id   bigint(20)      default 0                  comment '父菜单ID',
    order_num   int(4)          default 0                  comment '显示顺序',
    path        varchar(200) default '' comment '路由地址',
    component   varchar(255) default null comment '组件路径',
    query       varchar(255) default null comment '路由参数',
    route_name  varchar(50)  default '' comment '路由名称',
    is_frame    int(1)          default 1                  comment '是否为外链（0是 1否）',
    is_cache    int(1)          default 0                  comment '是否缓存（0缓存 1不缓存）',
    menu_type   char(1)      default '' comment '菜单类型（M目录 C菜单 F按钮）',
    visible     char(1)      default 0 comment '菜单状态（0显示 1隐藏）',
    status      char(1)      default 0 comment '菜单状态（0正常 1停用）',
    perms       varchar(100) default null comment '权限标识',
    icon        varchar(100) default '#' comment '菜单图标',
    create_by   varchar(64)  default '' comment '创建者',
    create_time datetime comment '创建时间',
    update_by   varchar(64)  default '' comment '更新者',
    update_time datetime comment '更新时间',
    remark      varchar(500) default '' comment '备注',
    primary key (menu_id)
) engine=innodb auto_increment=2000 comment = '菜单权限表';

-- ----------------------------
-- 用户和角色关联表  用户N-1角色
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role
(
    user_id bigint(20) not null comment '用户ID',
    role_id bigint(20) not null comment '角色ID',
    primary key (user_id, role_id)
) engine=innodb comment = '用户和角色关联表';

-- ----------------------------
-- 角色和菜单关联表  角色1-N菜单
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu
(
    role_id bigint(20) not null comment '角色ID',
    menu_id bigint(20) not null comment '菜单ID',
    primary key (role_id, menu_id)
) engine=innodb comment = '角色和菜单关联表';

-- ----------------------------
-- AI提示词表
-- ----------------------------
drop table if exists ai_prompt;
CREATE TABLE `ai_prompt`
(
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`         VARCHAR(255)          DEFAULT NULL COMMENT '提示词名称',
    `content`      MEDIUMTEXT   NOT NULL COMMENT '提示词内容',
    `model`        VARCHAR(128)          DEFAULT NULL COMMENT '模型，如 gpt-4o',
    `model_params` JSON                  DEFAULT NULL COMMENT '模型参数，如 {"temperature":0.7}',
    `version`      INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '版本号',
    `status`       TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `deleted`      TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除：0-未删除，1-已删除',
    `create_time`   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time`   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `key`           VARCHAR(100) NOT NULL COMMENT '唯一标识key',
    PRIMARY KEY (`id`),
    UNIQUE KEY      `ai_prompt_unique` (`key`),
    KEY            `idx_scene_status_deleted` (`status`, `deleted`),
    KEY            `idx_updated_at` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI提示词表';

-- ----------------------------
-- AI提示词表
-- ----------------------------
drop table if exists sys_auth_log;
create table sys_auth_log
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    user_id     bigint                             null comment '用户ID',
    username    varchar(50)                        not null comment '登录账号',
    login_type  varchar(20)                        not null comment '登录类型 LOGIN登录 LOGOUT退出',
    status      tinyint  default 0                 not null comment '状态 1成功 0失败',
    fail_reason varchar(255)                       null comment '失败原因',
    ip_address  varchar(64)                        null comment '登录IP',
    ip_location varchar(255)                       null comment 'IP归属地',
    user_agent  varchar(512)                       null comment '浏览器UA',
    uuid_id     varchar(128)                       null comment 'JWT Token唯一标识',
    login_time  datetime                           not null comment '登录时间',
    logout_time datetime                           null comment '退出时间',
    expire_time datetime                           null comment 'token过期时间',
    del_flag    char     default '0'               null comment '删除标志（0代表存在 1代表删除）',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '用户认证登录日志';

create index idx_ip_address
    on sys_auth_log (ip_address);

create index idx_login_time
    on sys_auth_log (login_time);

create index idx_status
    on sys_auth_log (status);

create index idx_user_id
    on sys_auth_log (user_id);

create index idx_username
    on sys_auth_log (username);

