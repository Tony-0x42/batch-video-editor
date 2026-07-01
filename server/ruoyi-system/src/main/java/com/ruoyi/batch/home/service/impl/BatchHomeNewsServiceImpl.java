package com.ruoyi.batch.home.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.batch.home.domain.BatchHomeNews;
import com.ruoyi.batch.home.mapper.BatchHomeNewsMapper;
import com.ruoyi.batch.home.service.IBatchHomeNewsService;

/**
 * 首页喜报数据Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class BatchHomeNewsServiceImpl implements IBatchHomeNewsService
{
    @Autowired
    private BatchHomeNewsMapper batchHomeNewsMapper;

    @Override
    public List<BatchHomeNews> selectBatchHomeNewsList(BatchHomeNews batchHomeNews)
    {
        return batchHomeNewsMapper.selectBatchHomeNewsList(batchHomeNews);
    }

    @Override
    public BatchHomeNews selectBatchHomeNewsById(Long newsId)
    {
        return batchHomeNewsMapper.selectBatchHomeNewsById(newsId);
    }

    @Override
    public int insertBatchHomeNews(BatchHomeNews batchHomeNews)
    {
        if (StringUtils.isEmpty(batchHomeNews.getStatus()))
        {
            batchHomeNews.setStatus("0");
        }
        batchHomeNews.setDelFlag("0");
        return batchHomeNewsMapper.insertBatchHomeNews(batchHomeNews);
    }

    @Override
    public int updateBatchHomeNews(BatchHomeNews batchHomeNews)
    {
        return batchHomeNewsMapper.updateBatchHomeNews(batchHomeNews);
    }

    @Override
    public int deleteBatchHomeNewsByIds(Long[] newsIds)
    {
        return batchHomeNewsMapper.deleteBatchHomeNewsByIds(newsIds);
    }
}
