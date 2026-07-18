package com.ruoyi.batch.aivideo.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoGroup;

/**
 * AI 云创视频组Mapper接口
 */
public interface BatchAiVideoGroupMapper
{
    /**
     * 查询视频组列表
     */
    public List<BatchAiVideoGroup> selectBatchAiVideoGroupList(BatchAiVideoGroup group);

    /**
     * 根据ID查询视频组
     */
    public BatchAiVideoGroup selectBatchAiVideoGroupById(Long groupId);

    /**
     * 新增视频组
     */
    public int insertBatchAiVideoGroup(BatchAiVideoGroup group);

    /**
     * 修改视频组
     */
    public int updateBatchAiVideoGroup(BatchAiVideoGroup group);

    /**
     * 删除视频组
     */
    public int deleteBatchAiVideoGroupById(Long groupId);

    /**
     * 增加已生成次数
     */
    public int incrementGeneratedCount(@Param("groupId") Long groupId, @Param("count") int count);
}
