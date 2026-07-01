package com.ruoyi.batch.home.service;

import java.util.List;
import com.ruoyi.batch.home.domain.BatchHomeDocumentOption;
import com.ruoyi.batch.home.domain.BatchHomeTutorialEntry;

/**
 * 首页教程入口Service接口
 *
 * @author ruoyi
 */
public interface IBatchHomeTutorialEntryService
{
    /**
     * 查询教程入口列表
     *
     * @param batchHomeTutorialEntry 查询条件
     * @return 结果列表
     */
    public List<BatchHomeTutorialEntry> selectBatchHomeTutorialEntryList(BatchHomeTutorialEntry batchHomeTutorialEntry);

    /**
     * 根据ID查询教程入口
     *
     * @param entryId 入口ID
     * @return 教程入口对象
     */
    public BatchHomeTutorialEntry selectBatchHomeTutorialEntryById(Long entryId);

    /**
     * 新增教程入口
     *
     * @param batchHomeTutorialEntry 教程入口对象
     * @return 影响行数
     */
    public int insertBatchHomeTutorialEntry(BatchHomeTutorialEntry batchHomeTutorialEntry);

    /**
     * 修改教程入口
     *
     * @param batchHomeTutorialEntry 教程入口对象
     * @return 影响行数
     */
    public int updateBatchHomeTutorialEntry(BatchHomeTutorialEntry batchHomeTutorialEntry);

    /**
     * 批量删除教程入口
     *
     * @param entryIds 入口ID数组
     * @return 影响行数
     */
    public int deleteBatchHomeTutorialEntryByIds(Long[] entryIds);

    /**
     * 查询可用的关联文档列表
     *
     * @return 文档选项列表
     */
    public List<BatchHomeDocumentOption> selectDocumentOptionList();
}
