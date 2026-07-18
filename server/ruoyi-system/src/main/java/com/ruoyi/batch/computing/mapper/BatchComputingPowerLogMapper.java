package com.ruoyi.batch.computing.mapper;

import java.util.List;
import com.ruoyi.batch.computing.domain.BatchComputingPowerLog;

/**
 * 算力消耗日志Mapper接口
 *
 * @author ruoyi
 */
public interface BatchComputingPowerLogMapper
{
    /**
     * 查询列表
     *
     * @param batchComputingPowerLog 查询条件
     * @return 结果列表
     */
    public List<BatchComputingPowerLog> selectList(BatchComputingPowerLog batchComputingPowerLog);

    /**
     * 新增算力消耗日志
     *
     * @param batchComputingPowerLog 算力消耗日志
     * @return 影响行数
     */
    public int insert(BatchComputingPowerLog batchComputingPowerLog);
}
