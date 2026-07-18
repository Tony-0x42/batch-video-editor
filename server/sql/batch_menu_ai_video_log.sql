-- ----------------------------
-- 批量剪辑 管理后台动态菜单：AI 视频生成记录
-- 目录：批量剪辑管理（parent_id = 3000，来自 sql/batch_menu.sql）
-- 菜单：生成记录（component: batch/aiVideoLog/index）
-- 权限字符串遵循 batch:模块:操作 格式
-- id 分配：菜单 3011（承接 batch_menu.sql 的 3000~3010），按钮权限 3190（承接 3100~3182）
-- ----------------------------

-- 生成记录（菜单）
insert into sys_menu values('3011', '生成记录', '3000', '11', 'aiVideoLog', 'batch/aiVideoLog/index', '', '', 1, 0, 'C', '0', '0', 'batch:aivideo:list', 'el-icon-video-camera', 'admin', sysdate(), '', null, 'AI视频生成记录菜单');

-- ----------------------------
-- 按钮权限
-- ----------------------------
insert into sys_menu values('3190', '记录查询', '3011', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'batch:aivideo:query', '#', 'admin', sysdate(), '', null, '');
