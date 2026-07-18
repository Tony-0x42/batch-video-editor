-- ------------------------------------------------------------
-- 批量剪辑：客户表增加头像字段
-- 背景：APP 端「账号资料编辑」支持上传头像并通过 PUT /batch/app/customer 提交 avatarUrl，
--      登录/用户信息接口直接返回 BatchCustomer 实体，需要该列持久化。
-- 执行方式：对已存在 batch_customer 表的环境执行本脚本；全新部署可直接用 batch_business.sql（已含此列）。
-- ------------------------------------------------------------

alter table batch_customer
    add column avatar_url varchar(500) default '' comment '头像地址' after vip_expire_date;
