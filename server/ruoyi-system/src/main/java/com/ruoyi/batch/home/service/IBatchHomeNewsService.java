package com.ruoyi.batch.home.service;

import java.util.List;
import com.ruoyi.batch.home.domain.BatchHomeNews;

/**
 * 首页喜报数据Service接口
 *
 * @author ruoyi
 */
public interface IBatchHomeNewsService
{
    /**
     * 查询喜报数据列表
     *
     * @param batchHomeNews 查询条件
     * @return 结果列表
     */
    public List<BatchHomeNews> selectBatchHomeNewsList(BatchHomeNews batchHomeNews);

    /**
     * 根据ID查询喜报数据
     *
     * @param newsId 喜报ID
     * @return 喜报对象
     */
    public BatchHomeNews selectBatchHomeNewsById(Long newsId);

    /**
     * 新增喜报数据
     *
     * @param batchHomeNews 喜报对象
     * @return 影响行数
     */
    public int insertBatchHomeNews(BatchHomeNews batchHomeNews);

    /**
     * 修改喜报数据
     *
     * @param batchHomeNews 喜报对象
     * @return 影响行数
     */
    public int updateBatchHomeNews(BatchHomeNews batchHomeNews);

    /**
     * 批量删除喜报数据
     *
     * @param newsIds 喜报ID数组
     * @return 影响行数
     */
    public int deleteBatchHomeNewsByIds(Long[] newsIds);
}
