package com.ruoyi.batch.config.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.batch.config.domain.BatchAppVersion;
import com.ruoyi.batch.config.mapper.BatchAppVersionMapper;
import com.ruoyi.batch.config.service.IBatchAppVersionService;

/**
 * APP 版本管理Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class BatchAppVersionServiceImpl implements IBatchAppVersionService
{
    @Autowired
    private BatchAppVersionMapper batchAppVersionMapper;

    @Override
    public List<BatchAppVersion> selectBatchAppVersionList(BatchAppVersion batchAppVersion)
    {
        return batchAppVersionMapper.selectBatchAppVersionList(batchAppVersion);
    }

    @Override
    public BatchAppVersion selectBatchAppVersionById(Long versionId)
    {
        return batchAppVersionMapper.selectBatchAppVersionById(versionId);
    }

    @Override
    public boolean checkVersionNoUnique(BatchAppVersion batchAppVersion)
    {
        BatchAppVersion exist = batchAppVersionMapper.checkVersionNoUnique(batchAppVersion);
        return exist == null;
    }

    @Override
    public int insertBatchAppVersion(BatchAppVersion batchAppVersion)
    {
        if (batchAppVersion.getPublishTime() == null)
        {
            batchAppVersion.setPublishTime(DateUtils.getNowDate());
        }
        return batchAppVersionMapper.insertBatchAppVersion(batchAppVersion);
    }

    @Override
    public int updateBatchAppVersion(BatchAppVersion batchAppVersion)
    {
        return batchAppVersionMapper.updateBatchAppVersion(batchAppVersion);
    }

    @Override
    public int deleteBatchAppVersionByIds(Long[] versionIds)
    {
        return batchAppVersionMapper.deleteBatchAppVersionByIds(versionIds);
    }
}
