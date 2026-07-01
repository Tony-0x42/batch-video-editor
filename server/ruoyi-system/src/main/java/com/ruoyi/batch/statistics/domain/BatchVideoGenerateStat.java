package com.ruoyi.batch.statistics.domain;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 视频生成明细
 *
 * @author ruoyi
 */
public class BatchVideoGenerateStat implements Serializable
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

    /** 视频组名称 */
    @Excel(name = "视频组名称", type = Excel.Type.EXPORT)
    private String videoGroupName;

    /** 生成数量 */
    @Excel(name = "生成数量", type = Excel.Type.EXPORT)
    private Integer generateCount;

    /** 状态：0 成功 / 1 失败 */
    @Excel(name = "状态", readConverterExp = "0=成功,1=失败", type = Excel.Type.EXPORT)
    private Integer status;

    /** 生成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "生成时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
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

    public String getVideoGroupName()
    {
        return videoGroupName;
    }

    public void setVideoGroupName(String videoGroupName)
    {
        this.videoGroupName = videoGroupName;
    }

    public Integer getGenerateCount()
    {
        return generateCount;
    }

    public void setGenerateCount(Integer generateCount)
    {
        this.generateCount = generateCount;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
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
