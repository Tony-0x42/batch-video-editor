package com.ruoyi.batch.home.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.batch.home.domain.BatchHomeEntry;
import com.ruoyi.batch.home.mapper.BatchHomeEntryMapper;
import com.ruoyi.batch.home.service.IBatchHomeEntryService;

/**
 * 首页功能入口Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class BatchHomeEntryServiceImpl implements IBatchHomeEntryService
{
    @Autowired
    private BatchHomeEntryMapper batchHomeEntryMapper;

    @Override
    public List<BatchHomeEntry> selectBatchHomeEntryList(BatchHomeEntry batchHomeEntry)
    {
        return batchHomeEntryMapper.selectBatchHomeEntryList(batchHomeEntry);
    }

    @Override
    public BatchHomeEntry selectBatchHomeEntryById(Long entryId)
    {
        return batchHomeEntryMapper.selectBatchHomeEntryById(entryId);
    }

    @Override
    public int insertBatchHomeEntry(BatchHomeEntry batchHomeEntry)
    {
        if (StringUtils.isEmpty(batchHomeEntry.getStatus()))
        {
            batchHomeEntry.setStatus("0");
        }
        batchHomeEntry.setDelFlag("0");
        return batchHomeEntryMapper.insertBatchHomeEntry(batchHomeEntry);
    }

    @Override
    public int updateBatchHomeEntry(BatchHomeEntry batchHomeEntry)
    {
        return batchHomeEntryMapper.updateBatchHomeEntry(batchHomeEntry);
    }

    @Override
    public int deleteBatchHomeEntryByIds(Long[] entryIds)
    {
        return batchHomeEntryMapper.deleteBatchHomeEntryByIds(entryIds);
    }
}
