package com.ruoyi.batch.home.mapper;

import java.util.List;
import com.ruoyi.batch.home.domain.BatchHomeEntry;

/**
 * 首页功能入口Mapper接口
 *
 * @author ruoyi
 */
public interface BatchHomeEntryMapper
{
    /**
     * 查询功能入口列表
     *
     * @param batchHomeEntry 查询条件
     * @return 结果列表
     */
    public List<BatchHomeEntry> selectBatchHomeEntryList(BatchHomeEntry batchHomeEntry);

    /**
     * 根据ID查询功能入口
     *
     * @param entryId 入口ID
     * @return 功能入口对象
     */
    public BatchHomeEntry selectBatchHomeEntryById(Long entryId);

    /**
     * 新增功能入口
     *
     * @param batchHomeEntry 功能入口对象
     * @return 影响行数
     */
    public int insertBatchHomeEntry(BatchHomeEntry batchHomeEntry);

    /**
     * 修改功能入口
     *
     * @param batchHomeEntry 功能入口对象
     * @return 影响行数
     */
    public int updateBatchHomeEntry(BatchHomeEntry batchHomeEntry);

    /**
     * 批量删除功能入口
     *
     * @param entryIds 入口ID数组
     * @return 影响行数
     */
    public int deleteBatchHomeEntryByIds(Long[] entryIds);
}
