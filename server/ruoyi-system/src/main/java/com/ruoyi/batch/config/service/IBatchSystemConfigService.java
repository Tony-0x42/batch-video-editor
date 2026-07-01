package com.ruoyi.batch.config.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.batch.config.domain.BatchSystemConfig;

/**
 * 扩展全局参数Service接口
 *
 * @author ruoyi
 */
public interface IBatchSystemConfigService
{
    /**
     * 查询扩展全局参数列表
     *
     * @param batchSystemConfig 扩展全局参数
     * @return 扩展全局参数集合
     */
    public List<BatchSystemConfig> selectBatchSystemConfigList(BatchSystemConfig batchSystemConfig);

    /**
     * 根据分组查询扩展全局参数Map
     *
     * @param configGroup 参数分组
     * @return 参数键值Map
     */
    public Map<String, String> selectBatchSystemConfigMapByGroup(String configGroup);

    /**
     * 根据参数键查询参数值
     *
     * @param configKey 参数键
     * @return 参数值
     */
    public String selectConfigValueByKey(String configKey);

    /**
     * 根据参数ID查询参数信息
     *
     * @param configId 参数ID
     * @return 扩展全局参数
     */
    public BatchSystemConfig selectBatchSystemConfigById(Long configId);

    /**
     * 批量保存分组参数
     *
     * @param configGroup 参数分组
     * @param configMap 参数键值
     * @param username 操作人
     * @return 结果
     */
    public int saveConfigGroup(String configGroup, Map<String, String> configMap, String username);

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
