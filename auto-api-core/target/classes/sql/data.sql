-- 初始化数据源配置
INSERT INTO `api_datasource_config` (`name`, `type`, `host`, `port`, `database`, `username`, `password`, `status`, `is_primary`, `remark`) VALUES
('测试数据库', 'mysql', 'localhost', 3306, 'auto_api', 'root', 'zk@12456', 1, 1, '本地测试数据源'),
('生产数据库', 'mysql', '192.168.1.100', 3306, 'prod_db', 'prod_user', 'prod_pass', 1, 0, '生产环境数据源');

-- 初始化表选择配置
INSERT INTO `api_table_selection` (`datasource_id`, `table_name`, `table_comment`, `selected`, `generate_mode`, `status`) VALUES
(1, 'users', '用户相关数据表', 1, 'auto', 1),
(1, 'orders', '订单相关数据表', 0, 'manual', 1),
(1, 'products', '产品相关数据表', 1, 'manual', 1),
(1, 'categories', '分类相关数据表', 1, 'auto', 1),
(1, 'comments', '评论相关数据表', 1, 'manual', 1),
(1, 'settings', '系统配置表', 0, 'manual', 1);

-- 初始化API生成状态
INSERT INTO `api_generation_status` (`api_path`, `datasource_id`, `table_name`, `status`, `rest_api_path`, `rest_api_registered`, `graphql_type`, `graphql_registered`) VALUES
('/api/users', 1, 'users', 'generated', '/api/users', 1, 'User', 1),
('/api/orders', 1, 'orders', 'skipped', NULL, 0, NULL, 0),
('/api/products', 1, 'products', 'pending', NULL, 0, NULL, 0),
('/api/categories', 1, 'categories', 'pending', NULL, 0, NULL, 0),
('/api/comments', 1, 'comments', 'generated', '/api/comments', 1, 'Comment', 1),
('/api/settings', 1, 'settings', 'skipped', NULL, 0, NULL, 0);