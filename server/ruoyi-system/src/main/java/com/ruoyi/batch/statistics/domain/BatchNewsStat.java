package com.ruoyi.batch.statistics.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 业绩喜报明细
 *
 * @author ruoyi
 */
public class BatchNewsStat implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 喜报ID */
    @Excel(name = "喜报ID", type = Excel.Type.EXPORT)
    private Long newsId;

    /** 业绩标题 */
    @Excel(name = "业绩标题", type = Excel.Type.EXPORT)
    private String newsTitle;

    /** 销售冠军姓名 */
    @Excel(name = "销售冠军", type = Excel.Type.EXPORT)
    private String championName;

    /** 销售金额 */
    @Excel(name = "销售金额", type = Excel.Type.EXPORT)
    private BigDecimal salesAmount;

    /** 状态：0 启用 / 1 禁用 */
    @Excel(name = "状态", readConverterExp = "0=启用,1=禁用", type = Excel.Type.EXPORT)
    private Integer status;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
    private Date updateTime;

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

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }
}
