package com.ruoyi.batch.vip.domain;

import com.ruoyi.batch.customer.domain.BatchCustomer;

/**
 * 会员 VIP 管理查询对象
 * 
 * @author ruoyi
 */
public class BatchVipQuery extends BatchCustomer
{
    private static final long serialVersionUID = 1L;

    /** 批量操作的客户ID数组 */
    private Long[] customerIds;

    public Long[] getCustomerIds()
    {
        return customerIds;
    }

    public void setCustomerIds(Long[] customerIds)
    {
        this.customerIds = customerIds;
    }
}
