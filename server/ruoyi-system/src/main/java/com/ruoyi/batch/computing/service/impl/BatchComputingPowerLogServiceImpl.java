package com.ruoyi.batch.computing.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.batch.computing.domain.BatchComputingPowerLog;
import com.ruoyi.batch.computing.mapper.BatchComputingPowerLogMapper;
import com.ruoyi.batch.computing.service.IBatchComputingPowerLogService;
import com.ruoyi.batch.customer.domain.BatchCustomer;
import com.ruoyi.batch.customer.mapper.BatchCustomerMapper;

/**
 * 算力消耗日志Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class BatchComputingPowerLogServiceImpl implements IBatchComputingPowerLogService
{
    /** 操作类型：1 生成 */
    private static final int OPERATION_TYPE_GENERATE = 1;

    @Autowired
    private BatchComputingPowerLogMapper batchComputingPowerLogMapper;

    @Autowired
    private BatchCustomerMapper batchCustomerMapper;

    @Override
    public List<BatchComputingPowerLog> selectList(BatchComputingPowerLog batchComputingPowerLog)
    {
        return batchComputingPowerLogMapper.selectList(batchComputingPowerLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal consumeComputingPower(String phone, Integer operationType, BigDecimal consumeValue, String bizNo)
    {
        if (consumeValue == null || consumeValue.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("消耗算力值必须大于 0");
        }

        // 原子扣减：单条带条件 UPDATE，余额不足时影响行数为 0，不记录日志
        int rows = batchCustomerMapper.consumeComputingPowerByPhone(phone, consumeValue);
        if (rows == 0)
        {
            BatchCustomer customer = batchCustomerMapper.selectBatchCustomerByPhone(phone);
            if (customer == null)
            {
                throw new ServiceException("账号不存在");
            }
            throw new ServiceException("当前算力已耗尽，请联系管理员增加算力额度");
        }

        BatchCustomer customer = batchCustomerMapper.selectBatchCustomerByPhone(phone);
        BigDecimal newRemain = customer.getComputingPowerRemain() != null
                ? customer.getComputingPowerRemain() : BigDecimal.ZERO;

        BatchComputingPowerLog log = new BatchComputingPowerLog();
        log.setPhone(phone);
        log.setOperationType(operationType);
        log.setConsumeValue(consumeValue);
        log.setRemainValue(newRemain);
        log.setVideoGroupName(bizNo);
        log.setCreateTime(DateUtils.getNowDate());
        batchComputingPowerLogMapper.insert(log);

        return newRemain;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal consumeComputingPower(String phone, BigDecimal consumeValue, String remark)
    {
        return consumeComputingPower(phone, OPERATION_TYPE_GENERATE, consumeValue, remark);
    }
}
