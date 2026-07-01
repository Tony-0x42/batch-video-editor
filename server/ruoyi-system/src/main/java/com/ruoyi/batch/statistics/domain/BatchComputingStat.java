package com.ruoyi.batch.statistics.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 算力消耗明细
 *
 * @author ruoyi
 */
public class BatchComputingStat implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    @Excel(name = "日志ID", type = Excel.Type.EXPORT)
    private Long logId;

    /** 账号手机号 */
    @Excel(name = "手机号", type = Excel.Type.EXPORT)
    private String phone;

    /** 账号名称 */
    @Excel(name = "账号名称", type = Excel.Type.EXPORT)
    private String customerName;

    /** 操作类型：1 生成 / 2 下载 */
    @Excel(name = "操作类型", readConverterExp = "1=生成,2=下载", type = Excel.Type.EXPORT)
    private Integer operationType;

    /** 消耗算力 */
    @Excel(name = "消耗算力", type = Excel.Type.EXPORT)
    private BigDecimal consumeValue;

    /** 剩余算力 */
    @Excel(name = "剩余算力", type = Excel.Type.EXPORT)
    private BigDecimal remainValue;

    /** 关联视频组 */
    @Excel(name = "关联视频组", type = Excel.Type.EXPORT)
    private String videoGroupName;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
    private Date createTime;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public Integer getOperationType()
    {
        return operationType;
    }

    public void setOperationType(Integer operationType)
    {
        this.operationType = operationType;
    }

    public BigDecimal getConsumeValue()
    {
        return consumeValue;
    }

    public void setConsumeValue(BigDecimal consumeValue)
    {
        this.consumeValue = consumeValue;
    }

    public BigDecimal getRemainValue()
    {
        return remainValue;
    }

    public void setRemainValue(BigDecimal remainValue)
    {
        this.remainValue = remainValue;
    }

    public String getVideoGroupName()
    {
        return videoGroupName;
    }

    public void setVideoGroupName(String videoGroupName)
    {
        this.videoGroupName = videoGroupName;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
