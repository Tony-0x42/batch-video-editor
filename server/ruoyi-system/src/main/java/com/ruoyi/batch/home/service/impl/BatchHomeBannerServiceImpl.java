package com.ruoyi.batch.home.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.batch.home.domain.BatchHomeBanner;
import com.ruoyi.batch.home.mapper.BatchHomeBannerMapper;
import com.ruoyi.batch.home.service.IBatchHomeBannerService;

/**
 * 首页轮播图Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class BatchHomeBannerServiceImpl implements IBatchHomeBannerService
{
    @Autowired
    private BatchHomeBannerMapper batchHomeBannerMapper;

    @Override
    public List<BatchHomeBanner> selectBatchHomeBannerList(BatchHomeBanner batchHomeBanner)
    {
        return batchHomeBannerMapper.selectBatchHomeBannerList(batchHomeBanner);
    }

    @Override
    public BatchHomeBanner selectBatchHomeBannerById(Long bannerId)
    {
        return batchHomeBannerMapper.selectBatchHomeBannerById(bannerId);
    }

    @Override
    public int insertBatchHomeBanner(BatchHomeBanner batchHomeBanner)
    {
        if (StringUtils.isEmpty(batchHomeBanner.getStatus()))
        {
            batchHomeBanner.setStatus("0");
        }
        batchHomeBanner.setDelFlag("0");
        return batchHomeBannerMapper.insertBatchHomeBanner(batchHomeBanner);
    }

    @Override
    public int updateBatchHomeBanner(BatchHomeBanner batchHomeBanner)
    {
        return batchHomeBannerMapper.updateBatchHomeBanner(batchHomeBanner);
    }

    @Override
    public int deleteBatchHomeBannerByIds(Long[] bannerIds)
    {
        return batchHomeBannerMapper.deleteBatchHomeBannerByIds(bannerIds);
    }
}
