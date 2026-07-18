-- ----------------------------
-- 批量剪辑 - AI 视频批量生成真实合成管线改造
-- 内容：batch_video_generate_log 扩展任务字段 + AI 生成成本全局参数
--
-- 注意：status 字段语义变更
--   旧语义：0 成功 / 1 失败（记账式假生成）
--   新语义：0 处理中 / 1 成功 / 2 失败（真实异步合成）
--   若库中存在旧数据，请先执行刷数（可选）：
--     update batch_video_generate_log set status = 1 where status = 0;
--     update batch_video_generate_log set status = 2 where status = 1 and 1 = 0; -- 旧失败数据极少，默认忽略
-- ----------------------------

alter table batch_video_generate_log
  add column group_id      bigint(20)      not null default 0  comment '所属视频组ID' after phone,
  add column progress      int(11)         not null default 0  comment '生成进度 0-100',
  add column result_url    varchar(500)    default ''          comment '产出视频访问URL',
  add column error_msg     varchar(500)    default ''          comment '失败原因',
  add column clip_seed     varchar(64)     default ''          comment '随机化种子（去重策略用）',
  add column consume_value decimal(18,2)   default 0.00        comment '本条产出消耗算力';

alter table batch_video_generate_log
  modify column status tinyint(1) not null default 0 comment '状态：0 处理中 / 1 成功 / 2 失败';

alter table batch_video_generate_log
  add key idx_batch_video_generate_log_group_id (group_id);

-- AI 视频生成单条成本（算力），不存在时后端默认按 1 处理
insert into batch_system_config (config_key, config_value, config_group, remark, create_time)
select 'batch.ai.generateCost', '1', 'ai', 'AI 视频生成单条消耗算力', now()
from dual
where not exists (select 1 from batch_system_config where config_key = 'batch.ai.generateCost');
