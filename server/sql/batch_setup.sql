-- 为 batchvideo 数据库初始化 APP 数据

-- 为 batch_customer 增加密码字段（如果不存在）
SET @exist := (SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_schema = 'batchvideo' AND table_name = 'batch_customer' AND column_name = 'password');
SET @sql := IF(@exist = 0, 'ALTER TABLE batch_customer ADD COLUMN password varchar(100) DEFAULT "" COMMENT "登录密码（BCrypt 加密）" AFTER phone', 'SELECT "password column already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 清空旧测试数据
DELETE FROM batch_home_banner WHERE create_by = 'admin';
DELETE FROM batch_home_entry WHERE create_by = 'admin';
DELETE FROM batch_home_news WHERE create_by = 'admin';
DELETE FROM batch_home_tutorial_entry WHERE create_by = 'admin';
DELETE FROM batch_tutorial WHERE create_by = 'admin';
DELETE FROM batch_tutorial_category WHERE create_by = 'admin';
DELETE FROM batch_document WHERE create_by = 'admin';
DELETE FROM batch_brand WHERE create_by = 'admin';
DELETE FROM batch_contact WHERE create_by = 'admin';

-- 首页轮播图
INSERT INTO batch_home_banner (title, image_url, link_url, sort_weight, status, del_flag, create_by, create_time) VALUES
('夏季新品大促', 'https://placehold.co/600x300/2196F3/FFFFFF?text=Banner1', '', 1, 0, 0, 'admin', NOW()),
('AI 去水印上新', 'https://placehold.co/600x300/1976D2/FFFFFF?text=Banner2', '', 2, 0, 0, 'admin', NOW()),
('品牌故事征集', 'https://placehold.co/600x300/64B5F6/FFFFFF?text=Banner3', '', 3, 0, 0, 'admin', NOW());

-- 首页功能入口菜单
INSERT INTO batch_home_entry (entry_name, icon_url, target_type, target_value, sort_weight, status, del_flag, create_by, create_time) VALUES
('学习专区', '', 1, 'learning', 1, 0, 0, 'admin', NOW()),
('信息咨询', '', 1, 'contact', 2, 0, 0, 'admin', NOW()),
('品牌专区', '', 1, 'brand', 3, 0, 0, 'admin', NOW()),
('其他服务', '', 0, '', 4, 0, 0, 'admin', NOW());

-- 首页喜报/新闻
INSERT INTO batch_home_news (news_title, champion_name, sales_amount, status, del_flag, create_by, create_time) VALUES
('恭喜华东区超额完成 Q2 目标！', '王小明', 586000, 0, 0, 'admin', NOW());

-- 首页新手指南入口
INSERT INTO batch_home_tutorial_entry (title, cover_url, document_id, sort_weight, status, del_flag, create_by, create_time) VALUES
('新手指南', 'https://placehold.co/600x200/2196F3/FFFFFF?text=Guide', 1, 1, 0, 0, 'admin', NOW());

-- 教程分类
INSERT INTO batch_tutorial_category (category_name, sort_weight, status, del_flag, create_by, create_time) VALUES
('全部', 0, 0, 0, 'admin', NOW()),
('视频', 1, 0, 0, 'admin', NOW()),
('文档', 2, 0, 0, 'admin', NOW()),
('PDF', 3, 0, 0, 'admin', NOW());

-- 教程内容 (type: 1 视频, 2 文档, 3 PDF)
INSERT INTO batch_tutorial (tutorial_title, tutorial_type, category_id, cover_url, video_url, document_content, intro, sort_weight, view_count, status, del_flag, create_by, create_time) VALUES
('快速入门：AI 去水印', 1, 2, 'https://placehold.co/200x120/2196F3/FFFFFF?text=Video1', '', '', '快速入门视频教程', 1, 0, 0, 0, 'admin', NOW()),
('账号层级说明', 2, 3, 'https://placehold.co/200x120/757575/FFFFFF?text=Doc1', '', '账号层级说明文档内容', '文档教程', 2, 0, 0, 0, 'admin', NOW()),
('批量剪辑 使用手册', 3, 4, 'https://placehold.co/200x120/FB8C00/FFFFFF?text=PDF1', '', '批量剪辑 使用手册 PDF 内容', 'PDF 手册', 3, 0, 0, 0, 'admin', NOW());

-- 文档 (document_type: 1 用户协议, 2 隐私政策, 3 新手指南, 4 帮助文档)
INSERT INTO batch_document (document_title, document_type, apply_pages, content, sort_weight, status, is_system, del_flag, create_by, create_time) VALUES
('如何快速去除视频水印', 3, 'document', '1. 复制短视频平台分享链接；\n2. 打开 AI 去水印页面粘贴链接；\n3. 点击解析并等待处理完成；\n4. 预览去水印结果后点击保存。', 1, 0, 0, 0, 'admin', NOW()),
('AI 云创分镜头管理', 3, 'document', '在 AI 云创页面，您可以创建视频组并上传素材，系统自动分割成分镜头。支持拖拽排序、镜像翻转和替换素材。', 2, 0, 0, 0, 'admin', NOW()),
('算力不足怎么办？', 4, 'document', '当算力耗尽时，视频生成与下载功能将被禁用，请联系管理员增加算力额度。', 3, 0, 0, 0, 'admin', NOW()),
('用户协议', 1, 'agreement', '欢迎使用批量剪辑。请遵守相关法律法规。', 1, 0, 1, 0, 'admin', NOW()),
('隐私政策', 2, 'privacy', '批量剪辑 重视您的个人信息保护。', 1, 0, 1, 0, 'admin', NOW());

-- 品牌
INSERT INTO batch_brand (brand_name, logo_url, intro, detail, media_urls, sort_weight, status, del_flag, create_by, create_time) VALUES
('批量剪辑', '', '批量剪辑 短视频创作品牌', '批量剪辑致力于通过 AI 技术降低短视频创作门槛。', 'https://placehold.co/400x300/2196F3/FFFFFF?text=Brand1', 1, 0, 0, 'admin', NOW()),
('星辰传媒', '', '品牌宣传与短视频运营服务商', '星辰传媒提供全链路短视频运营服务。', 'https://placehold.co/400x300/1976D2/FFFFFF?text=Brand2', 2, 0, 0, 'admin', NOW());

-- 联系方式 (contact_type: 1 在线客服, 2 总部热线, 3 区域联系)
INSERT INTO batch_contact (contact_name, region, phone, contact_type, sort_weight, status, del_flag, create_by, create_time) VALUES
('在线客服', '', '400-888-6666', 1, 1, 0, 0, 'admin', NOW()),
('总部热线', '', '400-888-8888', 2, 2, 0, 0, 'admin', NOW()),
('张伟', '华东区', '13800138001', 3, 3, 0, 0, 'admin', NOW()),
('李娜', '华北区', '13800138002', 3, 4, 0, 0, 'admin', NOW()),
('王强', '华南区', '13800138003', 3, 5, 0, 0, 'admin', NOW());
