package com.ruoyi.batch.statistics.domain;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 账号数据明细
 *
 * @author ruoyi
 */
public class BatchAccountStat implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 客户ID */
    @Excel(name = "客户ID", type = Excel.Type.EXPORT)
    private Long customerId;

    /** 账号类型：1 分公司 / 2 服务商 / 3 个人 */
    @Excel(name = "账号类型", readConverterExp = "1=分公司,2=服务商,3=个人", type = Excel.Type.EXPORT)
    private Integer customerType;

    /** 账号名称 */
    @Excel(name = "账号名称", type = Excel.Type.EXPORT)
    private String customerName;

    /** 联系人 */
    @Excel(name = "联系人", type = Excel.Type.EXPORT)
    private String contactName;

    /** 手机号 */
    @Excel(name = "手机号", type = Excel.Type.EXPORT)
    private String phone;

    /** 上级手机号 */
    @Excel(name = "上级手机号", type = Excel.Type.EXPORT)
    private String parentPhone;

    /** 所属分公司手机号 */
    @Excel(name = "所属分公司手机号", type = Excel.Type.EXPORT)
    private String branchPhone;

    /** 状态：0 启用 / 1 禁用 */
    @Excel(name = "状态", readConverterExp = "0=启用,1=禁用", type = Excel.Type.EXPORT)
    private Integer status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
    private Date createTime;

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public Integer getCustomerType()
    {
        return customerType;
    }

    public void setCustomerType(Integer customerType)
    {
        this.customerType = customerType;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public String getContactName()
    {
        return contactName;
    }

    public void setContactName(String contactName)
    {
        this.contactName = contactName;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getParentPhone()
    {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone)
    {
        this.parentPhone = parentPhone;
    }

    public String getBranchPhone()
    {
        return branchPhone;
    }

    public void setBranchPhone(String branchPhone)
    {
        this.branchPhone = branchPhone;
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
