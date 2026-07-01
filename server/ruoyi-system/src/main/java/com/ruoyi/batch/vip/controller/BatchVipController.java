package com.ruoyi.batch.vip.controller;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.batch.customer.domain.BatchCustomer;
import com.ruoyi.batch.vip.domain.BatchVipQuery;
import com.ruoyi.batch.vip.service.IBatchVipService;

/**
 * 会员 VIP 管理 信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/batch/vip")
public class BatchVipController extends BaseController
{
    @Autowired
    private IBatchVipService batchVipService;

    /**
     * 查询VIP客户列表
     */
    @PreAuthorize("@ss.hasPermi('batch:vip:list')")
    @GetMapping("/list")
    public TableDataInfo list(BatchVipQuery query)
    {
        startPage();
        List<BatchCustomer> list = batchVipService.selectBatchVipList(query);
        return getDataTable(list);
    }

    /**
     * 修改单个客户VIP有效期
     */
    @PreAuthorize("@ss.hasPermi('batch:vip:edit')")
    @Log(title = "会员VIP管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{customerId}")
    public AjaxResult edit(@PathVariable Long customerId, @RequestBody BatchVipQuery query)
    {
        Date vipExpireDate = query.getVipExpireDate();
        if (vipExpireDate == null)
        {
            return error("请选择VIP有效期");
        }
        return toAjax(batchVipService.updateVipExpireDate(customerId, vipExpireDate));
    }

    /**
     * 批量修改客户VIP有效期
     */
    @PreAuthorize("@ss.hasPermi('batch:vip:edit')")
    @Log(title = "会员VIP管理", businessType = BusinessType.UPDATE)
    @PutMapping("/batch")
    public AjaxResult editBatch(@RequestBody BatchVipQuery query)
    {
        Date vipExpireDate = query.getVipExpireDate();
        Long[] customerIds = query.getCustomerIds();
        if (customerIds == null || customerIds.length == 0)
        {
            return error("请至少选择一个账号");
        }
        if (vipExpireDate == null)
        {
            return error("请选择VIP有效期");
        }
        return toAjax(batchVipService.updateVipExpireDateBatch(customerIds, vipExpireDate));
    }
}
