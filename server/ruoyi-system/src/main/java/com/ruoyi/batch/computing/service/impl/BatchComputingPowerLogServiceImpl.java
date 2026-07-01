package com.ruoyi.batch.computing.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.batch.computing.domain.BatchComputingPowerLog;
import com.ruoyi.batch.computing.mapper.BatchComputingPowerLogMapper;
import com.ruoyi.batch.computing.service.IBatchComputingPowerLogService;

/**
 * computing业务模块Service业务层处理占位
 *
 * @author ruoyi
 */
@Service
public class BatchComputingPowerLogServiceImpl implements IBatchComputingPowerLogService
{
    @Autowired
    private BatchComputingPowerLogMapper batchComputingPowerLogMapper;

    @Override
    public List<BatchComputingPowerLog> selectList(BatchComputingPowerLog batchComputingPowerLog)
    {
        return batchComputingPowerLogMapper.selectList(batchComputingPowerLog);
    }
}
