package com.ruoyi.batch.customer.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.batch.customer.domain.BatchCustomer;
import com.ruoyi.batch.customer.service.IBatchCustomerService;

/**
 * 客户/APP账号管理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/batch/customer")
public class BatchCustomerController extends BaseController
{
    @Autowired
    private IBatchCustomerService batchCustomerService;

    /**
     * 查询客户列表
     */
    @PreAuthorize("@ss.hasPermi('batch:customer:list')")
    @GetMapping("/list")
    public TableDataInfo list(BatchCustomer batchCustomer)
    {
        // 分公司管理员仅查看本分公司下属数据
        if (!SecurityUtils.isAdmin())
        {
            String phone = SecurityUtils.getLoginUser().getUser().getPhonenumber();
            if (StringUtils.isNotEmpty(phone))
            {
                batchCustomer.setBranchPhone(phone);
            }
        }
        startPage();
        List<BatchCustomer> list = batchCustomerService.selectBatchCustomerList(batchCustomer);
        return getDataTable(list);
    }

    /**
     * 导出客户列表
     */
    @PreAuthorize("@ss.hasPermi('batch:customer:export')")
    @Log(title = "客户管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BatchCustomer batchCustomer)
    {
        if (!SecurityUtils.isAdmin())
        {
            String phone = SecurityUtils.getLoginUser().getUser().getPhonenumber();
            if (StringUtils.isNotEmpty(phone))
            {
                batchCustomer.setBranchPhone(phone);
            }
        }
        List<BatchCustomer> list = batchCustomerService.selectBatchCustomerList(batchCustomer);
        ExcelUtil<BatchCustomer> util = new ExcelUtil<BatchCustomer>(BatchCustomer.class);
        util.exportExcel(response, list, "客户数据");
    }

    /**
     * 根据ID获取客户详情
     */
    @PreAuthorize("@ss.hasPermi('batch:customer:query')")
    @GetMapping(value = { "/", "/{customerId}" })
    public AjaxResult getInfo(@PathVariable(value = "customerId", required = false) Long customerId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (customerId != null)
        {
            BatchCustomer customer = batchCustomerService.selectBatchCustomerById(customerId);
            ajax.put(AjaxResult.DATA_TAG, customer);
        }
        return ajax;
    }

    /**
     * 根据手机号查询客户
     */
    @PreAuthorize("@ss.hasPermi('batch:customer:query')")
    @GetMapping("/phone/{phone}")
    public AjaxResult getByPhone(@PathVariable("phone") String phone)
    {
        BatchCustomer customer = batchCustomerService.selectBatchCustomerByPhone(phone);
        return AjaxResult.success(customer);
    }

    /**
     * 新增客户
     */
    @PreAuthorize("@ss.hasPermi('batch:customer:add')")
    @Log(title = "客户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BatchCustomer batchCustomer)
    {
        batchCustomer.setCreateBy(getUsername());
        int rows = batchCustomerService.insertBatchCustomer(batchCustomer);
        AjaxResult ajax = toAjax(rows);
        ajax.put("customerId", batchCustomer.getCustomerId());
        ajax.put("qrCodeUrl", batchCustomer.getQrCodeUrl());
        return ajax;
    }

    /**
     * 修改客户
     */
    @PreAuthorize("@ss.hasPermi('batch:customer:edit')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BatchCustomer batchCustomer)
    {
        batchCustomer.setUpdateBy(getUsername());
        return toAjax(batchCustomerService.updateBatchCustomer(batchCustomer));
    }

    /**
     * 删除客户
     */
    @PreAuthorize("@ss.hasPermi('batch:customer:remove')")
    @Log(title = "客户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{customerIds}")
    public AjaxResult remove(@PathVariable Long[] customerIds)
    {
        return toAjax(batchCustomerService.deleteBatchCustomerByIds(customerIds));
    }

    /**
     * 修改客户状态
     */
    @PreAuthorize("@ss.hasPermi('batch:customer:edit')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody BatchCustomer batchCustomer)
    {
        batchCustomer.setUpdateBy(getUsername());
        return toAjax(batchCustomerService.updateStatus(batchCustomer));
    }

    /**
     * 生成/重置二维码
     */
    @PreAuthorize("@ss.hasPermi('batch:customer:resetQr')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/qrCode/{customerId}")
    public AjaxResult resetQrCode(@PathVariable("customerId") Long customerId)
    {
        String url = batchCustomerService.generateQrCode(customerId);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("qrCodeUrl", url);
        return ajax;
    }

    /**
     * 账号升级
     */
    @PreAuthorize("@ss.hasPermi('batch:customer:upgrade')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/upgrade/{customerId}")
    public AjaxResult upgrade(@PathVariable("customerId") Long customerId, @RequestBody BatchCustomer upgradeData)
    {
        return toAjax(batchCustomerService.upgradeCustomer(customerId, upgradeData.getParentPhone(),
                upgradeData.getMaxServiceProvider(), upgradeData.getTotalIndividualCapacity(),
                upgradeData.getMaxIndividual()));
    }

    /**
     * 账号迁移
     */
    @PreAuthorize("@ss.hasPermi('batch:customer:migrate')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/migrate/{customerId}")
    public AjaxResult migrate(@PathVariable("customerId") Long customerId, @RequestBody BatchCustomer migrateData)
    {
        return toAjax(batchCustomerService.migrateCustomer(customerId, migrateData.getParentPhone()));
    }
}
