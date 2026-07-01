package com.ruoyi.batch.statistics.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 数据统计查询参数
 *
 * @author ruoyi
 */
public class BatchStatisticsQuery extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /** 账号类型：1 分公司 / 2 服务商 / 3 个人 */
    private Integer customerType;

    /** 所属分公司手机号（总后台筛选用，分公司后台由后端自动填充） */
    private String branchPhone;

    /** 账号手机号（客户详情页按账号筛选） */
    private String phone;

    /** 趋势天数 */
    private Integer days;

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public Integer getCustomerType()
    {
        return customerType;
    }

    public void setCustomerType(Integer customerType)
    {
        this.customerType = customerType;
    }

    public String getBranchPhone()
    {
        return branchPhone;
    }

    public void setBranchPhone(String branchPhone)
    {
        this.branchPhone = branchPhone;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public Integer getDays()
    {
        return days;
    }

    public void setDays(Integer days)
    {
        this.days = days;
    }
}
