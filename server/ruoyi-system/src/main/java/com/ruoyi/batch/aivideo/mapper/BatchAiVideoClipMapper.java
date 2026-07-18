package com.ruoyi.batch.aivideo.mapper;

import java.util.List;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoClip;

/**
 * AI 云创分镜头Mapper接口
 */
public interface BatchAiVideoClipMapper
{
    /**
     * 根据视频组ID查询分镜头列表
     */
    public List<BatchAiVideoClip> selectBatchAiVideoClipByGroupId(Long groupId);

    /**
     * 新增分镜头
     */
    public int insertBatchAiVideoClip(BatchAiVideoClip clip);

    /**
     * 批量新增分镜头
     */
    public int insertBatchAiVideoClipBatch(java.util.List<BatchAiVideoClip> clips);

    /**
     * 修改分镜头
     */
    public int updateBatchAiVideoClip(BatchAiVideoClip clip);

    /**
     * 根据视频组ID删除分镜头
     */
    public int deleteBatchAiVideoClipByGroupId(Long groupId);
}
