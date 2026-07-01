-- ----------------------------
-- 批量剪辑 管理后台业务表
-- 模块：客户管理、VIP管理、首页内容、公告、教程、文档、算力、统计、系统配置
-- 状态约定：status 0 正常/启用/上架，1 停用/禁用/下架
-- 删除约定：del_flag 0 存在，2 删除
-- ----------------------------

-- ----------------------------
-- 1.1 账号体系扩展表
-- ----------------------------

-- 客户/APP 账号主表（与 sys_user 互补，后台管理员走 sys_user）
drop table if exists batch_customer;
create table batch_customer (
  customer_id               bigint(20)      not null auto_increment    comment '客户ID',
  customer_type             tinyint(1)      not null default 3         comment '账号类型：1 分公司 / 2 服务商 / 3 个人',
  customer_name             varchar(100)    not null default ''        comment '账号名称',
  contact_name              varchar(50)     default ''                 comment '联系人',
  phone                     varchar(20)     not null                   comment '手机号，全局唯一',
  parent_phone              varchar(20)     default ''                 comment '上级手机号',
  branch_phone              varchar(20)     default ''                 comment '所属分公司手机号（冗余，方便数据权限）',
  max_service_provider      int(11)         default 0                  comment '分公司最大可创建服务商数量',
  total_individual_capacity int(11)         default 0                  comment '分公司旗下服务商总可分配个人账号容量',
  max_individual            int(11)         default 0                  comment '服务商可拆分创建个人账号上限',
  computing_power_total     decimal(18,2)   default 0.00               comment '算力总配额 GF',
  computing_power_used      decimal(18,2)   default 0.00               comment '已消耗算力 GF',
  vip_expire_date           date            default null               comment 'VIP 有效期',
  qr_code_url               varchar(500)    default ''                 comment '注册二维码图片 URL',
  qr_code_key               varchar(100)    default ''                 comment '二维码唯一 key（重置时变更）',
  status                    tinyint(1)      not null default 0         comment '状态：0 启用 / 1 禁用',
  del_flag                  tinyint(1)      not null default 0         comment '删除标志：0 存在 / 2 删除',
  create_by                 varchar(64)     default ''                 comment '创建者',
  create_time               datetime        default current_timestamp  comment '创建时间',
  update_by                 varchar(64)     default ''                 comment '更新者',
  update_time               datetime        default current_timestamp  on update current_timestamp comment '更新时间',
  remark                    varchar(500)    default ''                 comment '备注',
  primary key (customer_id),
  unique key uk_batch_customer_phone (phone),
  key idx_batch_customer_parent_phone (parent_phone),
  key idx_batch_customer_branch_phone (branch_phone),
  key idx_batch_customer_customer_type (customer_type),
  key idx_batch_customer_status (status),
  key idx_batch_customer_del_flag (del_flag)
) engine=innodb auto_increment=1 comment = '客户/APP 账号主表';

-- 上下级关系快照表（可选，加速查询与迁移审计）
drop table if exists batch_customer_relation;
create table batch_customer_relation (
  relation_id               bigint(20)      not null auto_increment    comment '关系ID',
  parent_phone              varchar(20)     not null default ''        comment '上级手机号',
  child_phone               varchar(20)     not null default ''        comment '下级手机号',
  child_type                tinyint(1)      not null default 3         comment '下级类型：1 分公司 / 2 服务商 / 3 个人',
  create_time               datetime        default current_timestamp  comment '创建时间',
  primary key (relation_id),
  unique key uk_batch_customer_relation (parent_phone, child_phone),
  key idx_batch_customer_relation_child (child_phone),
  key idx_batch_customer_relation_child_type (child_type)
) engine=innodb auto_increment=1 comment = '上下级关系快照表';

-- ----------------------------
-- 1.2 首页内容表
-- ----------------------------

-- 首页轮播图
drop table if exists batch_home_banner;
create table batch_home_banner (
  banner_id                 bigint(20)      not null auto_increment    comment '轮播图ID',
  title                     varchar(200)    not null default ''        comment '标题',
  image_url                 varchar(500)    default ''                 comment '图片 URL',
  link_url                  varchar(500)    default ''                 comment '跳转链接',
  sort_weight               int(11)         default 0                  comment '排序权重',
  status                    tinyint(1)      not null default 0         comment '状态：0 启用 / 1 禁用',
  del_flag                  tinyint(1)      not null default 0         comment '删除标志：0 存在 / 2 删除',
  create_by                 varchar(64)     default ''                 comment '创建者',
  create_time               datetime        default current_timestamp  comment '创建时间',
  update_by                 varchar(64)     default ''                 comment '更新者',
  update_time               datetime        default current_timestamp  on update current_timestamp comment '更新时间',
  remark                    varchar(500)    default ''                 comment '备注',
  primary key (banner_id),
  key idx_batch_home_banner_status (status),
  key idx_batch_home_banner_del_flag (del_flag),
  key idx_batch_home_banner_sort_weight (sort_weight)
) engine=innodb auto_increment=1 comment = '首页轮播图表';

-- 首页喜报数据
drop table if exists batch_home_news;
create table batch_home_news (
  news_id                   bigint(20)      not null auto_increment    comment '喜报ID',
  news_title                varchar(200)    not null default ''        comment '业绩标题',
  champion_name             varchar(50)     default ''                 comment '销售冠军姓名',
  sales_amount              decimal(18,2)   default 0.00               comment '销售金额',
  status                    tinyint(1)      not null default 0         comment '状态：0 启用 / 1 禁用',
  del_flag                  tinyint(1)      not null default 0         comment '删除标志：0 存在 / 2 删除',
  create_by                 varchar(64)     default ''                 comment '创建者',
  create_time               datetime        default current_timestamp  comment '创建时间',
  update_by                 varchar(64)     default ''                 comment '更新者',
  update_time               datetime        default current_timestamp  on update current_timestamp comment '更新时间',
  remark                    varchar(500)    default ''                 comment '备注',
  primary key (news_id),
  key idx_batch_home_news_status (status),
  key idx_batch_home_news_del_flag (del_flag)
) engine=innodb auto_increment=1 comment = '首页喜报数据表';

-- 首页功能入口
drop table if exists batch_home_entry;
create table batch_home_entry (
  entry_id                  bigint(20)      not null auto_increment    comment '入口ID',
  entry_name                varchar(100)    not null default ''        comment '入口名称',
  icon_url                  varchar(500)    default ''                 comment '图标 URL',
  target_type               tinyint(1)      not null default 1         comment '跳转类型：1 页面 / 2 URL / 3 功能码',
  target_value              varchar(500)    default ''                 comment '跳转目标值',
  sort_weight               int(11)         default 0                  comment '排序权重',
  status                    tinyint(1)      not null default 0         comment '状态：0 启用 / 1 禁用',
  del_flag                  tinyint(1)      not null default 0         comment '删除标志：0 存在 / 2 删除',
  create_by                 varchar(64)     default ''                 comment '创建者',
  create_time               datetime        default current_timestamp  comment '创建时间',
  update_by                 varchar(64)     default ''                 comment '更新者',
  update_time               datetime        default current_timestamp  on update current_timestamp comment '更新时间',
  remark                    varchar(500)    default ''                 comment '备注',
  primary key (entry_id),
  key idx_batch_home_entry_status (status),
  key idx_batch_home_entry_del_flag (del_flag),
  key idx_batch_home_entry_sort_weight (sort_weight)
) engine=innodb auto_increment=1 comment = '首页功能入口表';

-- 首页教程入口
drop table if exists batch_home_tutorial_entry;
create table batch_home_tutorial_entry (
  entry_id                  bigint(20)      not null auto_increment    comment '入口ID',
  title                     varchar(100)    not null default ''        comment '入口标题',
  cover_url                 varchar(500)    default ''                 comment '封面图 URL',
  document_id               bigint(20)      default 0                  comment '关联文档 ID',
  sort_weight               int(11)         default 0                  comment '排序权重',
  status                    tinyint(1)      not null default 0         comment '状态：0 启用 / 1 禁用',
  del_flag                  tinyint(1)      not null default 0         comment '删除标志：0 存在 / 2 删除',
  create_by                 varchar(64)     default ''                 comment '创建者',
  create_time               datetime        default current_timestamp  comment '创建时间',
  update_by                 varchar(64)     default ''                 comment '更新者',
  update_time               datetime        default current_timestamp  on update current_timestamp comment '更新时间',
  remark                    varchar(500)    default ''                 comment '备注',
  primary key (entry_id),
  key idx_batch_home_tutorial_entry_document (document_id),
  key idx_batch_home_tutorial_entry_status (status),
  key idx_batch_home_tutorial_entry_del_flag (del_flag),
  key idx_batch_home_tutorial_entry_sort_weight (sort_weight)
) engine=innodb auto_increment=1 comment = '首页教程入口表';

-- ----------------------------
-- 1.3 公告/教程/文档表
-- ----------------------------

-- APP 公告
drop table if exists batch_app_notice;
create table batch_app_notice (
  notice_id                 bigint(20)      not null auto_increment    comment '公告ID',
  notice_title              varchar(200)    not null default ''        comment '标题',
  notice_type               tinyint(1)      not null default 1         comment '公告类型：1 通知 / 2 活动 / 3 重要更新',
  cover_url                 varchar(500)    default ''                 comment '封面图 URL',
  content                   longtext                                   comment '富文本内容',
  publish_status            tinyint(1)      not null default 2         comment '发布状态：0 已发布 / 1 已下架 / 2 暂存',
  publish_time              datetime        default null               comment '发布时间',
  read_count                int(11)         default 0                  comment '阅读量',
  del_flag                  tinyint(1)      not null default 0         comment '删除标志：0 存在 / 2 删除',
  create_by                 varchar(64)     default ''                 comment '创建者',
  create_time               datetime        default current_timestamp  comment '创建时间',
  update_by                 varchar(64)     default ''                 comment '更新者',
  update_time               datetime        default current_timestamp  on update current_timestamp comment '更新时间',
  remark                    varchar(500)    default ''                 comment '备注',
  primary key (notice_id),
  key idx_batch_app_notice_type (notice_type),
  key idx_batch_app_notice_publish_status (publish_status),
  key idx_batch_app_notice_del_flag (del_flag),
  key idx_batch_app_notice_publish_time (publish_time)
) engine=innodb auto_increment=1 comment = 'APP 公告表';

-- 教程
drop table if exists batch_tutorial;
create table batch_tutorial (
  tutorial_id               bigint(20)      not null auto_increment    comment '教程ID',
  tutorial_title            varchar(200)    not null default ''        comment '标题',
  tutorial_type             tinyint(1)      not null default 1         comment '教程类型：1 视频 / 2 图文',
  category_id               bigint(20)      default 0                  comment '分类 ID',
  cover_url                 varchar(500)    default ''                 comment '封面图 URL',
  video_url                 varchar(500)    default ''                 comment '视频文件 URL',
  document_content          longtext                                   comment '图文内容',
  intro                     varchar(500)    default ''                 comment '简介',
  sort_weight               int(11)         default 0                  comment '排序权重',
  view_count                int(11)         default 0                  comment '浏览量',
  status                    tinyint(1)      not null default 0         comment '状态：0 上架 / 1 下架',
  del_flag                  tinyint(1)      not null default 0         comment '删除标志：0 存在 / 2 删除',
  create_by                 varchar(64)     default ''                 comment '创建者',
  create_time               datetime        default current_timestamp  comment '创建时间',
  update_by                 varchar(64)     default ''                 comment '更新者',
  update_time               datetime        default current_timestamp  on update current_timestamp comment '更新时间',
  remark                    varchar(500)    default ''                 comment '备注',
  primary key (tutorial_id),
  key idx_batch_tutorial_category (category_id),
  key idx_batch_tutorial_type (tutorial_type),
  key idx_batch_tutorial_status (status),
  key idx_batch_tutorial_del_flag (del_flag),
  key idx_batch_tutorial_sort_weight (sort_weight)
) engine=innodb auto_increment=1 comment = '教程表';

-- 教程分类
drop table if exists batch_tutorial_category;
create table batch_tutorial_category (
  category_id               bigint(20)      not null auto_increment    comment '分类ID',
  category_name             varchar(100)    not null default ''        comment '分类名',
  sort_weight               int(11)         default 0                  comment '排序权重',
  status                    tinyint(1)      not null default 0         comment '状态：0 启用 / 1 禁用',
  del_flag                  tinyint(1)      not null default 0         comment '删除标志：0 存在 / 2 删除',
  create_by                 varchar(64)     default ''                 comment '创建者',
  create_time               datetime        default current_timestamp  comment '创建时间',
  update_by                 varchar(64)     default ''                 comment '更新者',
  update_time               datetime        default current_timestamp  on update current_timestamp comment '更新时间',
  remark                    varchar(500)    default ''                 comment '备注',
  primary key (category_id),
  key idx_batch_tutorial_category_status (status),
  key idx_batch_tutorial_category_del_flag (del_flag),
  key idx_batch_tutorial_category_sort_weight (sort_weight)
) engine=innodb auto_increment=1 comment = '教程分类表';

-- 文档管理
drop table if exists batch_document;
create table batch_document (
  document_id               bigint(20)      not null auto_increment    comment '文档ID',
  document_title            varchar(200)    not null default ''        comment '标题',
  document_type             tinyint(1)      not null default 1         comment '文档类型：1 用户协议 / 2 隐私政策 / 3 新手文档 / 4 帮助文档',
  apply_pages               varchar(200)    default ''                 comment '适用页面，逗号分隔',
  content                   longtext                                   comment '富文本内容',
  sort_weight               int(11)         default 0                  comment '排序权重',
  status                    tinyint(1)      not null default 0         comment '状态：0 启用 / 1 禁用',
  is_system                 tinyint(1)      default 0                  comment '是否系统默认：1 系统默认不可删除 / 0 否',
  del_flag                  tinyint(1)      not null default 0         comment '删除标志：0 存在 / 2 删除',
  create_by                 varchar(64)     default ''                 comment '创建者',
  create_time               datetime        default current_timestamp  comment '创建时间',
  update_by                 varchar(64)     default ''                 comment '更新者',
  update_time               datetime        default current_timestamp  on update current_timestamp comment '更新时间',
  remark                    varchar(500)    default ''                 comment '备注',
  primary key (document_id),
  key idx_batch_document_type (document_type),
  key idx_batch_document_status (status),
  key idx_batch_document_del_flag (del_flag),
  key idx_batch_document_sort_weight (sort_weight)
) engine=innodb auto_increment=1 comment = '文档管理表';

-- ----------------------------
-- 1.4 算力/统计/配置表
-- ----------------------------

-- 算力消耗日志
drop table if exists batch_computing_power_log;
create table batch_computing_power_log (
  log_id                    bigint(20)      not null auto_increment    comment '日志ID',
  phone                     varchar(20)     not null default ''        comment '账号手机号',
  operation_type            tinyint(1)      not null default 1         comment '操作类型：1 生成 / 2 下载',
  consume_value             decimal(18,2)   default 0.00               comment '消耗算力',
  remain_value              decimal(18,2)   default 0.00               comment '剩余算力',
  video_group_name          varchar(200)    default ''                 comment '关联视频组',
  create_time               datetime        default current_timestamp  comment '创建时间',
  primary key (log_id),
  key idx_batch_computing_power_log_phone (phone),
  key idx_batch_computing_power_log_operation_type (operation_type),
  key idx_batch_computing_power_log_create_time (create_time)
) engine=innodb auto_increment=1 comment = '算力消耗日志表';

-- 视频生成记录
drop table if exists batch_video_generate_log;
create table batch_video_generate_log (
  log_id                    bigint(20)      not null auto_increment    comment '日志ID',
  phone                     varchar(20)     not null default ''        comment '账号手机号',
  video_group_name          varchar(200)    default ''                 comment '视频组名称',
  generate_count            int(11)         default 0                  comment '生成数量',
  status                    tinyint(1)      not null default 0         comment '状态：0 成功 / 1 失败',
  create_time               datetime        default current_timestamp  comment '创建时间',
  primary key (log_id),
  key idx_batch_video_generate_log_phone (phone),
  key idx_batch_video_generate_log_status (status),
  key idx_batch_video_generate_log_create_time (create_time)
) engine=innodb auto_increment=1 comment = '视频生成记录表';

-- 二维码推广统计
drop table if exists batch_qr_code_stat;
create table batch_qr_code_stat (
  stat_id                   bigint(20)      not null auto_increment    comment '统计ID',
  phone                     varchar(20)     not null default ''        comment '二维码所属账号手机号',
  scan_count                int(11)         default 0                  comment '扫码次数',
  download_count            int(11)         default 0                  comment '下载次数',
  register_count            int(11)         default 0                  comment '注册次数',
  stat_date                 date            not null                   comment '统计日期',
  primary key (stat_id),
  unique key uk_batch_qr_code_stat_phone_date (phone, stat_date),
  key idx_batch_qr_code_stat_stat_date (stat_date)
) engine=innodb auto_increment=1 comment = '二维码推广统计表';

-- 扩展全局参数
drop table if exists batch_system_config;
create table batch_system_config (
  config_id                 bigint(20)      not null auto_increment    comment '参数ID',
  config_key                varchar(100)    not null default ''        comment '参数键，如 batch.ai.maxVideos',
  config_value              varchar(500)    default ''                 comment '参数值',
  config_group              varchar(50)     default ''                 comment '分组：brand/global/ai',
  remark                    varchar(500)    default ''                 comment '备注',
  create_by                 varchar(64)     default ''                 comment '创建者',
  create_time               datetime        default current_timestamp  comment '创建时间',
  update_by                 varchar(64)     default ''                 comment '更新者',
  update_time               datetime        default current_timestamp  on update current_timestamp comment '更新时间',
  primary key (config_id),
  unique key uk_batch_system_config_key (config_key),
  key idx_batch_system_config_group (config_group)
) engine=innodb auto_increment=1 comment = '扩展全局参数表';

-- APP 版本管理
drop table if exists batch_app_version;
create table batch_app_version (
  version_id                bigint(20)      not null auto_increment    comment '版本ID',
  version_no                varchar(50)     not null default ''        comment '版本号',
  platform                  tinyint(1)      not null default 1         comment '平台：1 Android / 2 iOS',
  update_type               tinyint(1)      not null default 2         comment '更新类型：1 强制 / 2 提示 / 3 静默',
  update_content            text                                       comment '更新内容',
  download_url              varchar(500)    default ''                 comment '下载地址',
  publish_time              datetime        default null               comment '发布时间',
  status                    tinyint(1)      not null default 0         comment '状态：0 启用 / 1 禁用',
  del_flag                  tinyint(1)      not null default 0         comment '删除标志：0 存在 / 2 删除',
  create_by                 varchar(64)     default ''                 comment '创建者',
  create_time               datetime        default current_timestamp  comment '创建时间',
  update_by                 varchar(64)     default ''                 comment '更新者',
  update_time               datetime        default current_timestamp  on update current_timestamp comment '更新时间',
  remark                    varchar(500)    default ''                 comment '备注',
  primary key (version_id),
  key idx_batch_app_version_platform (platform),
  key idx_batch_app_version_status (status),
  key idx_batch_app_version_del_flag (del_flag)
) engine=innodb auto_increment=1 comment = 'APP 版本管理表';
