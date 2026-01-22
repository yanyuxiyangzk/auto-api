-- 数据源配置表
CREATE TABLE IF NOT EXISTS `api_datasource_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(100) NOT NULL COMMENT '数据源名称',
    `type` varchar(50) NOT NULL COMMENT '数据源类型: mysql/postgresql/oracle/sqlserver',
    `host` varchar(255) NOT NULL COMMENT '主机地址',
    `port` int NOT NULL COMMENT '端口号',
    `database` varchar(100) NOT NULL COMMENT '数据库名称',
    `schema_name` varchar(100) DEFAULT NULL COMMENT '模式名称(PostgreSQL)',
    `username` varchar(100) NOT NULL COMMENT '用户名',
    `password` varchar(255) NOT NULL COMMENT '密码',
    `connection_params` varchar(500) DEFAULT NULL COMMENT '连接参数',
    `pool_config` varchar(1000) DEFAULT NULL COMMENT '连接池配置',
    `status` tinyint DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `is_primary` tinyint DEFAULT 0 COMMENT '是否主数据源: 0-否 1-是',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` varchar(100) DEFAULT NULL COMMENT '更新人',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据源配置表';

-- 初始化一条测试数据
INSERT INTO `api_datasource_config` (`name`, `type`, `host`, `port`, `database`, `username`, `password`, `status`, `is_primary`, `remark`)
VALUES ('测试数据库', 'mysql', 'localhost', 3306, 'test_db', 'root', '123456', 1, 1, '本地测试数据库');
