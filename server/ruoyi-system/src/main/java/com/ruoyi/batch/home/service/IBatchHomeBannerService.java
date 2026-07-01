package com.ruoyi.batch.home.service;

import java.util.List;
import com.ruoyi.batch.home.domain.BatchHomeBanner;

/**
 * 首页轮播图Service接口
 *
 * @author ruoyi
 */
public interface IBatchHomeBannerService
{
    /**
     * 查询轮播图列表
     *
     * @param batchHomeBanner 查询条件
     * @return 结果列表
     */
    public List<BatchHomeBanner> selectBatchHomeBannerList(BatchHomeBanner batchHomeBanner);

    /**
     * 根据ID查询轮播图
     *
     * @param bannerId 轮播图ID
     * @return 轮播图对象
     */
    public BatchHomeBanner selectBatchHomeBannerById(Long bannerId);

    /**
     * 新增轮播图
     *
     * @param batchHomeBanner 轮播图对象
     * @return 影响行数
     */
    public int insertBatchHomeBanner(BatchHomeBanner batchHomeBanner);

    /**
     * 修改轮播图
     *
     * @param batchHomeBanner 轮播图对象
     * @return 影响行数
     */
    public int updateBatchHomeBanner(BatchHomeBanner batchHomeBanner);

    /**
     * 批量删除轮播图
     *
     * @param bannerIds 轮播图ID数组
     * @return 影响行数
     */
    public int deleteBatchHomeBannerByIds(Long[] bannerIds);
}
