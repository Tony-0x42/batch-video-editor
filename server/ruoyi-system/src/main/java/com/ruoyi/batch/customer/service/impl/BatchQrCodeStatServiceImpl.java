package com.ruoyi.batch.customer.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.batch.customer.mapper.BatchQrCodeStatMapper;
import com.ruoyi.batch.customer.service.IBatchQrCodeStatService;

/**
 * 二维码推广统计Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class BatchQrCodeStatServiceImpl implements IBatchQrCodeStatService
{
    @Autowired
    private BatchQrCodeStatMapper qrCodeStatMapper;

    @Override
    public void incrementScanCount(String phone)
    {
        if (StringUtils.isNotEmpty(phone))
        {
            qrCodeStatMapper.incrementScanCount(phone);
        }
    }

    @Override
    public void incrementDownloadCount(String phone)
    {
        if (StringUtils.isNotEmpty(phone))
        {
            qrCodeStatMapper.incrementDownloadCount(phone);
        }
    }

    @Override
    public void incrementRegisterCount(String phone)
    {
        if (StringUtils.isNotEmpty(phone))
        {
            qrCodeStatMapper.incrementRegisterCount(phone);
        }
    }

    @Override
    public java.util.Map<String, Object> selectTotalsByPhone(String phone)
    {
        java.util.Map<String, Object> totals = StringUtils.isEmpty(phone) ? null : qrCodeStatMapper.selectTotalsByPhone(phone);
        if (totals == null)
        {
            totals = new java.util.HashMap<>();
            totals.put("scanCount", 0);
            totals.put("downloadCount", 0);
            totals.put("registerCount", 0);
        }
        return totals;
    }
}
