package com.ruoyi.batch.statistics.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.batch.statistics.domain.BatchQrCodeStat;
import com.ruoyi.batch.statistics.mapper.BatchQrCodeStatMapper;
import com.ruoyi.batch.statistics.service.IBatchQrCodeStatService;

/**
 * statistics业务模块Service业务层处理占位
 *
 * @author ruoyi
 */
@Service
public class BatchQrCodeStatServiceImpl implements IBatchQrCodeStatService
{
    @Autowired
    private BatchQrCodeStatMapper batchQrCodeStatMapper;

    @Override
    public List<BatchQrCodeStat> selectList(BatchQrCodeStat batchQrCodeStat)
    {
        return batchQrCodeStatMapper.selectList(batchQrCodeStat);
    }
}
