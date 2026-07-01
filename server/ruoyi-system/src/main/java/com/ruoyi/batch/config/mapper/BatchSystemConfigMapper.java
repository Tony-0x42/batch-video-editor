package com.ruoyi.batch.config.mapper;

import java.util.List;
import com.ruoyi.batch.config.domain.BatchSystemConfig;

/**
 * 扩展全局参数Mapper接口
 *
 * @author ruoyi
 */
public interface BatchSystemConfigMapper
{
    /**
     * 查询扩展全局参数列表
     *
     * @param batchSystemConfig 扩展全局参数
     * @return 扩展全局参数集合
     */
    public List<BatchSystemConfig> selectBatchSystemConfigList(BatchSystemConfig batchSystemConfig);

    /**
     * 根据分组查询扩展全局参数列表
     *
     * @param configGroup 参数分组
     * @return 扩展全局参数集合
     */
    public List<BatchSystemConfig> selectBatchSystemConfigByGroup(String configGroup);

    /**
     * 根据参数键查询参数信息
     *
     * @param configKey 参数键
     * @return 扩展全局参数
     */
    public BatchSystemConfig selectBatchSystemConfigByKey(String configKey);

    /**
     * 根据参数ID查询参数信息
     *
     * @param configId 参数ID
     * @return 扩展全局参数
     */
    public BatchSystemConfig selectBatchSystemConfigById(Long configId);

    /**
     * 新增扩展全局参数
     *
     * @param batchSystemConfig 扩展全局参数
     * @return 结果
     */
    public int insertBatchSystemConfig(BatchSystemConfig batchSystemConfig);

    /**
     * 修改扩展全局参数
     *
     * @param batchSystemConfig 扩展全局参数
     * @return 结果
     */
    public int updateBatchSystemConfig(BatchSystemConfig batchSystemConfig);

    /**
     * 批量删除扩展全局参数
     *
     * @param configIds 需要删除的参数ID集合
     * @return 结果
     */
    public int deleteBatchSystemConfigByIds(Long[] configIds);
}
