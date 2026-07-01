package com.ruoyi.batch.home.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.batch.home.domain.BatchHomeDocumentOption;
import com.ruoyi.batch.home.domain.BatchHomeTutorialEntry;
import com.ruoyi.batch.home.mapper.BatchHomeTutorialEntryMapper;
import com.ruoyi.batch.home.service.IBatchHomeTutorialEntryService;

/**
 * 首页教程入口Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class BatchHomeTutorialEntryServiceImpl implements IBatchHomeTutorialEntryService
{
    @Autowired
    private BatchHomeTutorialEntryMapper batchHomeTutorialEntryMapper;

    @Override
    public List<BatchHomeTutorialEntry> selectBatchHomeTutorialEntryList(BatchHomeTutorialEntry batchHomeTutorialEntry)
    {
        return batchHomeTutorialEntryMapper.selectBatchHomeTutorialEntryList(batchHomeTutorialEntry);
    }

    @Override
    public BatchHomeTutorialEntry selectBatchHomeTutorialEntryById(Long entryId)
    {
        return batchHomeTutorialEntryMapper.selectBatchHomeTutorialEntryById(entryId);
    }

    @Override
    public int insertBatchHomeTutorialEntry(BatchHomeTutorialEntry batchHomeTutorialEntry)
    {
        if (StringUtils.isEmpty(batchHomeTutorialEntry.getStatus()))
        {
            batchHomeTutorialEntry.setStatus("0");
        }
        batchHomeTutorialEntry.setDelFlag("0");
        return batchHomeTutorialEntryMapper.insertBatchHomeTutorialEntry(batchHomeTutorialEntry);
    }

    @Override
    public int updateBatchHomeTutorialEntry(BatchHomeTutorialEntry batchHomeTutorialEntry)
    {
        return batchHomeTutorialEntryMapper.updateBatchHomeTutorialEntry(batchHomeTutorialEntry);
    }

    @Override
    public int deleteBatchHomeTutorialEntryByIds(Long[] entryIds)
    {
        return batchHomeTutorialEntryMapper.deleteBatchHomeTutorialEntryByIds(entryIds);
    }

    @Override
    public List<BatchHomeDocumentOption> selectDocumentOptionList()
    {
        return batchHomeTutorialEntryMapper.selectDocumentOptionList();
    }
}
