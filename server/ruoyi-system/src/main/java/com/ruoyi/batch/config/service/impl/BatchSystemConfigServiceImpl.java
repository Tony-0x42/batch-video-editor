package com.ruoyi.batch.config.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.batch.config.domain.BatchSystemConfig;
import com.ruoyi.batch.config.mapper.BatchSystemConfigMapper;
import com.ruoyi.batch.config.service.IBatchSystemConfigService;

/**
 * 扩展全局参数Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class BatchSystemConfigServiceImpl implements IBatchSystemConfigService
{
    @Autowired
    private BatchSystemConfigMapper batchSystemConfigMapper;

    @Override
    public List<BatchSystemConfig> selectBatchSystemConfigList(BatchSystemConfig batchSystemConfig)
    {
        return batchSystemConfigMapper.selectBatchSystemConfigList(batchSystemConfig);
    }

    @Override
    public Map<String, String> selectBatchSystemConfigMapByGroup(String configGroup)
    {
        List<BatchSystemConfig> list = batchSystemConfigMapper.selectBatchSystemConfigByGroup(configGroup);
        Map<String, String> map = new HashMap<>();
        for (BatchSystemConfig config : list)
        {
            map.put(config.getConfigKey(), config.getConfigValue());
        }
        return map;
    }

    @Override
    public String selectConfigValueByKey(String configKey)
    {
        BatchSystemConfig config = batchSystemConfigMapper.selectBatchSystemConfigByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public BatchSystemConfig selectBatchSystemConfigById(Long configId)
    {
        return batchSystemConfigMapper.selectBatchSystemConfigById(configId);
    }

    @Override
    @Transactional
    public int saveConfigGroup(String configGroup, Map<String, String> configMap, String username)
    {
        if (configMap == null || configMap.isEmpty())
        {
            return 0;
        }
        int count = 0;
        for (Map.Entry<String, String> entry : configMap.entrySet())
        {
            String configKey = entry.getKey();
            String configValue = entry.getValue();
            if (StringUtils.isEmpty(configKey))
            {
                continue;
            }
            BatchSystemConfig exist = batchSystemConfigMapper.selectBatchSystemConfigByKey(configKey);
            if (exist != null)
            {
                BatchSystemConfig update = new BatchSystemConfig();
                update.setConfigId(exist.getConfigId());
                update.setConfigValue(configValue);
                update.setConfigGroup(configGroup);
                update.setUpdateBy(username);
                count += batchSystemConfigMapper.updateBatchSystemConfig(update);
            }
            else
            {
                BatchSystemConfig insert = new BatchSystemConfig();
                insert.setConfigKey(configKey);
                insert.setConfigValue(configValue);
                insert.setConfigGroup(configGroup);
                insert.setCreateBy(username);
                insert.setRemark("");
                count += batchSystemConfigMapper.insertBatchSystemConfig(insert);
            }
        }
        return count;
    }

    @Override
    public int insertBatchSystemConfig(BatchSystemConfig batchSystemConfig)
    {
        return batchSystemConfigMapper.insertBatchSystemConfig(batchSystemConfig);
    }

    @Override
    public int updateBatchSystemConfig(BatchSystemConfig batchSystemConfig)
    {
        return batchSystemConfigMapper.updateBatchSystemConfig(batchSystemConfig);
    }

    @Override
    public int deleteBatchSystemConfigByIds(Long[] configIds)
    {
        return batchSystemConfigMapper.deleteBatchSystemConfigByIds(configIds);
    }
}
