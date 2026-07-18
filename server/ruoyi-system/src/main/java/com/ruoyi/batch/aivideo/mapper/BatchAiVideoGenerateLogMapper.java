package com.ruoyi.batch.aivideo.mapper;

import java.util.List;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoGenerateLog;

/**
 * AI 视频生成记录Mapper接口
 */
public interface BatchAiVideoGenerateLogMapper
{
    /**
     * 查询生成记录列表
     */
    public List<BatchAiVideoGenerateLog> selectBatchAiVideoGenerateLogList(BatchAiVideoGenerateLog query);

    /**
     * 根据ID查询生成记录
     */
    public BatchAiVideoGenerateLog selectBatchAiVideoGenerateLogById(Long logId);

    /**
     * 新增生成记录
     */
    public int insertBatchAiVideoGenerateLog(BatchAiVideoGenerateLog log);

    /**
     * 更新生成记录（进度/状态/结果/失败原因）
     */
    public int updateBatchAiVideoGenerateLog(BatchAiVideoGenerateLog log);
}
