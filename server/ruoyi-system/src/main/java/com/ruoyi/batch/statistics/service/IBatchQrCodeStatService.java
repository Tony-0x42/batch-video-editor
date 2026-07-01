package com.ruoyi.batch.statistics.service;

import java.util.List;
import com.ruoyi.batch.statistics.domain.BatchQrCodeStat;

/**
 * statistics业务模块Service接口占位
 *
 * @author ruoyi
 */
public interface IBatchQrCodeStatService
{
    /**
     * 查询列表
     *
     * @param batchQrCodeStat 查询条件
     * @return 结果列表
     */
    public List<BatchQrCodeStat> selectList(BatchQrCodeStat batchQrCodeStat);
}
