package com.ruoyi.batch.computing.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.batch.computing.domain.BatchComputingPowerLog;

/**
 * 算力消耗日志Service接口
 *
 * @author ruoyi
 */
public interface IBatchComputingPowerLogService
{
    /**
     * 查询列表
     *
     * @param batchComputingPowerLog 查询条件
     * @return 结果列表
     */
    public List<BatchComputingPowerLog> selectList(BatchComputingPowerLog batchComputingPowerLog);

    /**
     * 检查并消耗算力
     *
     * @param phone 账号手机号
     * @param operationType 操作类型：1 生成 / 2 下载
     * @param consumeValue 消耗算力值
     * @param bizNo 业务单号/说明
     * @return 消耗后剩余算力
     */
    public BigDecimal consumeComputingPower(String phone, Integer operationType, BigDecimal consumeValue, String bizNo);

    /**
     * 检查并消耗算力（操作类型默认 1 生成）
     *
     * @param phone 账号手机号
     * @param consumeValue 消耗算力值
     * @param remark 业务单号/说明
     * @return 消耗后剩余算力
     */
    public BigDecimal consumeComputingPower(String phone, BigDecimal consumeValue, String remark);
}
