package com.ruoyi.batch.config.mapper;

import java.util.List;
import com.ruoyi.batch.config.domain.BatchAppVersion;

/**
 * APP 版本管理Mapper接口
 *
 * @author ruoyi
 */
public interface BatchAppVersionMapper
{
    /**
     * 查询APP版本列表
     *
     * @param batchAppVersion APP版本
     * @return APP版本集合
     */
    public List<BatchAppVersion> selectBatchAppVersionList(BatchAppVersion batchAppVersion);

    /**
     * 根据版本ID查询APP版本
     *
     * @param versionId 版本ID
     * @return APP版本
     */
    public BatchAppVersion selectBatchAppVersionById(Long versionId);

    /**
     * 根据版本号查询APP版本
     *
     * @param batchAppVersion APP版本
     * @return APP版本
     */
    public BatchAppVersion checkVersionNoUnique(BatchAppVersion batchAppVersion);

    /**
     * 新增APP版本
     *
     * @param batchAppVersion APP版本
     * @return 结果
     */
    public int insertBatchAppVersion(BatchAppVersion batchAppVersion);

    /**
     * 修改APP版本
     *
     * @param batchAppVersion APP版本
     * @return 结果
     */
    public int updateBatchAppVersion(BatchAppVersion batchAppVersion);

    /**
     * 批量删除APP版本
     *
     * @param versionIds 需要删除的版本ID集合
     * @return 结果
     */
    public int deleteBatchAppVersionByIds(Long[] versionIds);
}
