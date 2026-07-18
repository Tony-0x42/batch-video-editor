package com.ruoyi.batch.aivideo.service;

import java.util.List;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoClip;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoGenerateBody;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoGenerateLog;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoGroup;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoSplitBody;

/**
 * AI 云创视频服务接口
 */
public interface IBatchAiVideoService
{
    /**
     * 查询当前账号视频组列表
     */
    public List<BatchAiVideoGroup> selectBatchAiVideoGroupList(String phone);

    /**
     * 根据ID查询视频组（含分镜头）
     */
    public BatchAiVideoGroup selectBatchAiVideoGroupById(Long groupId);

    /**
     * 新增视频组
     */
    public int insertBatchAiVideoGroup(BatchAiVideoGroup group);

    /**
     * 修改视频组（含分镜头覆盖保存）
     */
    public int updateBatchAiVideoGroup(BatchAiVideoGroup group);

    /**
     * 删除视频组（含分镜头）
     */
    public int deleteBatchAiVideoGroupById(Long groupId);

    /**
     * AI 分割：将已上传视频按指定时长切段并覆盖保存为该组分镜头
     *
     * @param phone 当前账号手机号
     * @param body  分割请求
     * @return 切段生成的分镜头列表
     */
    public List<BatchAiVideoClip> splitVideo(String phone, BatchAiVideoSplitBody body);

    /**
     * 提交 AI 视频批量生成任务（异步合成）
     *
     * @param phone 当前账号手机号
     * @param body  生成请求（count 为生成数量，默认1，上限10）
     * @return 本批次生成记录 logId 列表
     */
    public List<Long> submitGenerate(String phone, BatchAiVideoGenerateBody body);

    /**
     * 查询生成记录列表（APP 任务轮询 / 管理后台分页）
     */
    public List<BatchAiVideoGenerateLog> selectBatchAiVideoGenerateLogList(BatchAiVideoGenerateLog query);

    /**
     * 根据ID查询生成记录
     */
    public BatchAiVideoGenerateLog selectBatchAiVideoGenerateLogById(Long logId);
}
