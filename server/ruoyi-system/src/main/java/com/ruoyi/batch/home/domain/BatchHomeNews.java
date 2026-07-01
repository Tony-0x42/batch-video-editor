package com.ruoyi.batch.home.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 首页喜报数据对象 batch_home_news
 *
 * @author ruoyi
 */
public class BatchHomeNews extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 喜报ID */
    private Long newsId;

    /** 业绩标题 */
    @Excel(name = "业绩标题")
    private String newsTitle;

    /** 销售冠军姓名 */
    @Excel(name = "销售冠军")
    private String championName;

    /** 销售金额 */
    @Excel(name = "销售金额")
    private BigDecimal salesAmount;

    /** 状态：0 启用 / 1 禁用 */
    @Excel(name = "状态", readConverterExp = "0=启用,1=禁用")
    private String status;

    /** 删除标志：0 存在 / 2 删除 */
    private String delFlag;

    public Long getNewsId()
    {
        return newsId;
    }

    public void setNewsId(Long newsId)
    {
        this.newsId = newsId;
    }

    public String getNewsTitle()
    {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle)
    {
        this.newsTitle = newsTitle;
    }

    public String getChampionName()
    {
        return championName;
    }

    public void setChampionName(String championName)
    {
        this.championName = championName;
    }

    public BigDecimal getSalesAmount()
    {
        return salesAmount;
    }

    public void setSalesAmount(BigDecimal salesAmount)
    {
        this.salesAmount = salesAmount;
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
            .append("newsId", getNewsId())
            .append("newsTitle", getNewsTitle())
            .append("championName", getChampionName())
            .append("salesAmount", getSalesAmount())
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
