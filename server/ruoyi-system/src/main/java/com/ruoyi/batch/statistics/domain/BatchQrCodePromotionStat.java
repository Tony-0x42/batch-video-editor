package com.ruoyi.batch.statistics.domain;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 二维码推广明细
 *
 * @author ruoyi
 */
public class BatchQrCodePromotionStat implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 统计ID */
    @Excel(name = "统计ID", type = Excel.Type.EXPORT)
    private Long statId;

    /** 二维码所属账号手机号 */
    @Excel(name = "手机号", type = Excel.Type.EXPORT)
    private String phone;

    /** 账号名称 */
    @Excel(name = "账号名称", type = Excel.Type.EXPORT)
    private String customerName;

    /** 账号类型 */
    @Excel(name = "账号类型", readConverterExp = "1=分公司,2=服务商,3=个人", type = Excel.Type.EXPORT)
    private Integer customerType;

    /** 扫码次数 */
    @Excel(name = "扫码次数", type = Excel.Type.EXPORT)
    private Long scanCount;

    /** 下载次数 */
    @Excel(name = "下载次数", type = Excel.Type.EXPORT)
    private Long downloadCount;

    /** 注册次数 */
    @Excel(name = "注册次数", type = Excel.Type.EXPORT)
    private Long registerCount;

    /** 统计日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "统计日期", width = 20, dateFormat = "yyyy-MM-dd", type = Excel.Type.EXPORT)
    private Date statDate;

    public Long getStatId()
    {
        return statId;
    }

    public void setStatId(Long statId)
    {
        this.statId = statId;
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

    public Integer getCustomerType()
    {
        return customerType;
    }

    public void setCustomerType(Integer customerType)
    {
        this.customerType = customerType;
    }

    public Long getScanCount()
    {
        return scanCount;
    }

    public void setScanCount(Long scanCount)
    {
        this.scanCount = scanCount;
    }

    public Long getDownloadCount()
    {
        return downloadCount;
    }

    public void setDownloadCount(Long downloadCount)
    {
        this.downloadCount = downloadCount;
    }

    public Long getRegisterCount()
    {
        return registerCount;
    }

    public void setRegisterCount(Long registerCount)
    {
        this.registerCount = registerCount;
    }

    public Date getStatDate()
    {
        return statDate;
    }

    public void setStatDate(Date statDate)
    {
        this.statDate = statDate;
    }
}
