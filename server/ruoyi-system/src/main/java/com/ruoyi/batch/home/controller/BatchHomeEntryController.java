package com.ruoyi.batch.home.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.batch.home.domain.BatchHomeEntry;
import com.ruoyi.batch.home.service.IBatchHomeEntryService;

/**
 * 首页功能入口Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/batch/home/entry")
public class BatchHomeEntryController extends BaseController
{
    @Autowired
    private IBatchHomeEntryService entryService;

    /**
     * 查询首页功能入口列表
     */
    @PreAuthorize("@ss.hasPermi('batch:home:list')")
    @GetMapping("/list")
    public TableDataInfo list(BatchHomeEntry entry)
    {
        startPage();
        List<BatchHomeEntry> list = entryService.selectBatchHomeEntryList(entry);
        return getDataTable(list);
    }

    /**
     * 导出首页功能入口列表
     */
    @PreAuthorize("@ss.hasPermi('batch:home:export')")
    @Log(title = "首页功能入口", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BatchHomeEntry entry)
    {
        List<BatchHomeEntry> list = entryService.selectBatchHomeEntryList(entry);
        ExcelUtil<BatchHomeEntry> util = new ExcelUtil<BatchHomeEntry>(BatchHomeEntry.class);
        util.exportExcel(response, list, "首页功能入口数据");
    }

    /**
     * 获取首页功能入口详细信息
     */
    @PreAuthorize("@ss.hasPermi('batch:home:query')")
    @GetMapping(value = "/{entryId}")
    public AjaxResult getInfo(@PathVariable("entryId") Long entryId)
    {
        return success(entryService.selectBatchHomeEntryById(entryId));
    }

    /**
     * 新增首页功能入口
     */
    @PreAuthorize("@ss.hasPermi('batch:home:add')")
    @Log(title = "首页功能入口", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BatchHomeEntry entry)
    {
        entry.setCreateBy(getUsername());
        return toAjax(entryService.insertBatchHomeEntry(entry));
    }

    /**
     * 修改首页功能入口
     */
    @PreAuthorize("@ss.hasPermi('batch:home:edit')")
    @Log(title = "首页功能入口", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BatchHomeEntry entry)
    {
        entry.setUpdateBy(getUsername());
        return toAjax(entryService.updateBatchHomeEntry(entry));
    }

    /**
     * 修改首页功能入口状态
     */
    @PreAuthorize("@ss.hasPermi('batch:home:edit')")
    @Log(title = "首页功能入口", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody BatchHomeEntry entry)
    {
        BatchHomeEntry exist = entryService.selectBatchHomeEntryById(entry.getEntryId());
        if (exist == null)
        {
            return error("数据不存在");
        }
        exist.setStatus(entry.getStatus());
        exist.setUpdateBy(getUsername());
        return toAjax(entryService.updateBatchHomeEntry(exist));
    }

    /**
     * 删除首页功能入口
     */
    @PreAuthorize("@ss.hasPermi('batch:home:remove')")
    @Log(title = "首页功能入口", businessType = BusinessType.DELETE)
    @DeleteMapping("/{entryIds}")
    public AjaxResult remove(@PathVariable Long[] entryIds)
    {
        entryService.deleteBatchHomeEntryByIds(entryIds);
        return success();
    }
}
