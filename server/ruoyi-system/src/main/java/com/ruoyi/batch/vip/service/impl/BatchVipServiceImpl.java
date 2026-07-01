package com.ruoyi.batch.vip.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.batch.customer.domain.BatchCustomer;
import com.ruoyi.batch.customer.mapper.BatchCustomerMapper;
import com.ruoyi.batch.vip.domain.BatchVipQuery;
import com.ruoyi.batch.vip.service.IBatchVipService;

/**
 * 会员 VIP 管理Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class BatchVipServiceImpl implements IBatchVipService
{
    @Autowired
    private BatchCustomerMapper batchCustomerMapper;

    @Override
    public List<BatchCustomer> selectBatchVipList(BatchVipQuery query)
    {
        return batchCustomerMapper.selectBatchCustomerList(query);
    }

    @Override
    public int updateVipExpireDate(Long customerId, Date vipExpireDate)
    {
        return batchCustomerMapper.updateVipExpireDate(customerId, vipExpireDate);
    }

    @Override
    public int updateVipExpireDateBatch(Long[] customerIds, Date vipExpireDate)
    {
        return batchCustomerMapper.updateVipExpireDateBatch(customerIds, vipExpireDate);
    }
}
