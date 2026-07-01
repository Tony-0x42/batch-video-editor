package com.ruoyi.batch.home.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 首页功能入口对象 batch_home_entry
 *
 * @author ruoyi
 */
public class BatchHomeEntry extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 入口ID */
    private Long entryId;

    /** 入口名称 */
    @Excel(name = "入口名称")
    private String entryName;

    /** 图标URL */
    @Excel(name = "图标URL")
    private String iconUrl;

    /** 跳转类型：1 页面 / 2 URL / 3 功能码 */
    @Excel(name = "跳转类型", readConverterExp = "1=页面,2=URL,3=功能码")
    private String targetType;

    /** 跳转目标值 */
    @Excel(name = "跳转目标")
    private String targetValue;

    /** 排序权重 */
    @Excel(name = "排序权重")
    private Integer sortWeight;

    /** 状态：0 启用 / 1 禁用 */
    @Excel(name = "状态", readConverterExp = "0=启用,1=禁用")
    private String status;

    /** 删除标志：0 存在 / 2 删除 */
    private String delFlag;

    public Long getEntryId()
    {
        return entryId;
    }

    public void setEntryId(Long entryId)
    {
        this.entryId = entryId;
    }

    public String getEntryName()
    {
        return entryName;
    }

    public void setEntryName(String entryName)
    {
        this.entryName = entryName;
    }

    public String getIconUrl()
    {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl)
    {
        this.iconUrl = iconUrl;
    }

    public String getTargetType()
    {
        return targetType;
    }

    public void setTargetType(String targetType)
    {
        this.targetType = targetType;
    }

    public String getTargetValue()
    {
        return targetValue;
    }

    public void setTargetValue(String targetValue)
    {
        this.targetValue = targetValue;
    }

    public Integer getSortWeight()
    {
        return sortWeight;
    }

    public void setSortWeight(Integer sortWeight)
    {
        this.sortWeight = sortWeight;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("entryId", getEntryId())
            .append("entryName", getEntryName())
            .append("iconUrl", getIconUrl())
            .append("targetType", getTargetType())
            .append("targetValue", getTargetValue())
            .append("sortWeight", getSortWeight())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
