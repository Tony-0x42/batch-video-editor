package com.ruoyi.batch.vip.service;

import java.util.Date;
import java.util.List;
import com.ruoyi.batch.customer.domain.BatchCustomer;
import com.ruoyi.batch.vip.domain.BatchVipQuery;

/**
 * 会员 VIP 管理Service接口
 * 
 * @author ruoyi
 */
public interface IBatchVipService
{
    /**
     * 查询VIP客户列表
     * 
     * @param query 查询条件
     * @return 结果列表
     */
    public List<BatchCustomer> selectBatchVipList(BatchVipQuery query);

    /**
     * 修改单个客户VIP有效期
     * 
     * @param customerId 客户ID
     * @param vipExpireDate VIP有效期
     * @return 结果
     */
    public int updateVipExpireDate(Long customerId, Date vipExpireDate);

    /**
     * 批量修改客户VIP有效期
     * 
     * @param customerIds 客户ID数组
     * @param vipExpireDate VIP有效期
     * @return 结果
     */
    public int updateVipExpireDateBatch(Long[] customerIds, Date vipExpireDate);
}
