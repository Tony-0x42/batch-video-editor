-- ----------------------------
-- 批量剪辑 管理后台动态菜单
-- 目录：批量剪辑管理
-- 菜单：客户管理 / 会员 VIP 管理 / 首页内容管理 / 公告管理 / 教程管理 / 文档管理 / 数据统计 / 系统配置
-- 权限字符串遵循 batch:模块:操作 格式
-- ----------------------------

-- 批量剪辑管理（目录）
insert into sys_menu values('3000', '批量剪辑管理', '0', '5', 'batch', null, '', '', 1, 0, 'M', '0', '0', '', 'el-icon-s-management', 'admin', sysdate(), '', null, '批量剪辑管理目录');

-- 二级菜单
insert into sys_menu values('3001', '客户管理',     '3000', '1', 'customer',   'batch/customer/index',    '', '', 1, 0, 'C', '0', '0', 'batch:customer:list',    'el-icon-user',       'admin', sysdate(), '', null, '客户管理菜单');
insert into sys_menu values('3002', '会员VIP管理',  '3000', '2', 'vip',        'batch/vip/index',         '', '', 1, 0, 'C', '0', '0', 'batch:vip:list',         'el-icon-medal-1',    'admin', sysdate(), '', null, '会员VIP管理菜单');
insert into sys_menu values('3003', '首页内容管理', '3000', '3', 'home',       'batch/home/index',        '', '', 1, 0, 'C', '0', '0', 'batch:home:list',        'el-icon-house',      'admin', sysdate(), '', null, '首页内容管理菜单');
insert into sys_menu values('3004', '公告管理',     '3000', '4', 'notice',     'batch/notice/index',      '', '', 1, 0, 'C', '0', '0', 'batch:notice:list',      'el-icon-bell',       'admin', sysdate(), '', null, '公告管理菜单');
insert into sys_menu values('3005', '教程管理',     '3000', '5', 'tutorial',   'batch/tutorial/index',    '', '', 1, 0, 'C', '0', '0', 'batch:tutorial:list',    'el-icon-reading',    'admin', sysdate(), '', null, '教程管理菜单');
insert into sys_menu values('3006', '文档管理',     '3000', '6', 'document',   'batch/document/index',    '', '', 1, 0, 'C', '0', '0', 'batch:document:list',    'el-icon-document',   'admin', sysdate(), '', null, '文档管理菜单');
insert into sys_menu values('3007', '数据统计',     '3000', '7', 'statistics', 'batch/statistics/index',  '', '', 1, 0, 'C', '0', '0', 'batch:statistics:list',  'el-icon-data-line',  'admin', sysdate(), '', null, '数据统计菜单');
insert into sys_menu values('3008', '系统配置',     '3000', '8', 'config',     'batch/config/index',      '', '', 1, 0, 'C', '0', '0', 'batch:config:list',      'el-icon-setting',    'admin', sysdate(), '', null, '系统配置菜单');

-- ----------------------------
-- 按钮权限
-- ----------------------------

-- 客户管理按钮权限
insert into sys_menu values('3100', '客户查询',   '3001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:customer:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3101', '客户新增',   '3001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:customer:add',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3102', '客户修改',   '3001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:customer:edit',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3103', '客户删除',   '3001', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:customer:remove',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3104', '客户导出',   '3001', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:customer:export',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3105', '客户升级',   '3001', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:customer:upgrade', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3106', '客户迁移',   '3001', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:customer:migrate', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3107', '重置二维码', '3001', '8', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:customer:resetQr', '#', 'admin', sysdate(), '', null, '');

-- 会员 VIP 管理按钮权限
insert into sys_menu values('3110', 'VIP查询', '3002', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:vip:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3111', 'VIP修改', '3002', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:vip:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3112', 'VIP导出', '3002', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:vip:export', '#', 'admin', sysdate(), '', null, '');

-- 首页内容管理按钮权限
insert into sys_menu values('3120', '首页内容查询', '3003', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:home:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3121', '首页内容新增', '3003', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:home:add',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3122', '首页内容修改', '3003', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:home:edit',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3123', '首页内容删除', '3003', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:home:remove',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3124', '首页内容导出', '3003', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:home:export',  '#', 'admin', sysdate(), '', null, '');

-- 公告管理按钮权限
insert into sys_menu values('3130', '公告查询', '3004', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:notice:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3131', '公告新增', '3004', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:notice:add',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3132', '公告修改', '3004', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:notice:edit',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3133', '公告删除', '3004', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:notice:remove',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3134', '公告导出', '3004', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:notice:export',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3135', '公告发布', '3004', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:notice:publish', '#', 'admin', sysdate(), '', null, '');

-- 教程管理按钮权限
insert into sys_menu values('3140', '教程查询', '3005', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:tutorial:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3141', '教程新增', '3005', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:tutorial:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3142', '教程修改', '3005', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:tutorial:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3143', '教程删除', '3005', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:tutorial:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3144', '教程导出', '3005', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:tutorial:export', '#', 'admin', sysdate(), '', null, '');

-- 文档管理按钮权限
insert into sys_menu values('3150', '文档查询', '3006', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:document:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3151', '文档新增', '3006', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:document:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3152', '文档修改', '3006', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:document:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3153', '文档删除', '3006', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:document:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3154', '文档导出', '3006', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:document:export', '#', 'admin', sysdate(), '', null, '');

-- 数据统计按钮权限
insert into sys_menu values('3160', '统计查询', '3007', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:statistics:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3161', '统计导出', '3007', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:statistics:export', '#', 'admin', sysdate(), '', null, '');

-- 系统配置按钮权限
insert into sys_menu values('3170', '配置查询', '3008', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:config:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3171', '配置新增', '3008', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:config:add',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3172', '配置修改', '3008', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:config:edit',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3173', '配置删除', '3008', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:config:remove',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3174', '配置导出', '3008', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:config:export',  '#', 'admin', sysdate(), '', null, '');
