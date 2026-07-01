package com.ruoyi.batch.config.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 扩展全局参数表 batch_system_config
 *
 * @author ruoyi
 */
public class BatchSystemConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 参数ID */
    private Long configId;

    /** 参数键，如 batch.ai.maxVideos */
    private String configKey;

    /** 参数值 */
    private String configValue;

    /** 分组：brand/global/ai */
    private String configGroup;

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    @NotBlank(message = "参数键不能为空")
    @Size(min = 0, max = 100, message = "参数键长度不能超过100个字符")
    public String getConfigKey()
    {
        return configKey;
    }

    public void setConfigKey(String configKey)
    {
        this.configKey = configKey;
    }

    @NotBlank(message = "参数值不能为空")
    @Size(min = 0, max = 500, message = "参数值长度不能超过500个字符")
    public String getConfigValue()
    {
        return configValue;
    }

    public void setConfigValue(String configValue)
    {
        this.configValue = configValue;
    }

    @Size(min = 0, max = 50, message = "参数分组长度不能超过50个字符")
    public String getConfigGroup()
    {
        return configGroup;
    }

    public void setConfigGroup(String configGroup)
    {
        this.configGroup = configGroup;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("configKey", getConfigKey())
            .append("configValue", getConfigValue())
            .append("configGroup", getConfigGroup())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
