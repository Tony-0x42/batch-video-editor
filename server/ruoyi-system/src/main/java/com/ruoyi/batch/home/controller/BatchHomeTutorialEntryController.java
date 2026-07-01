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
import com.ruoyi.batch.home.domain.BatchHomeDocumentOption;
import com.ruoyi.batch.home.domain.BatchHomeTutorialEntry;
import com.ruoyi.batch.home.service.IBatchHomeTutorialEntryService;

/**
 * 首页教程入口Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/batch/home/tutorialEntry")
public class BatchHomeTutorialEntryController extends BaseController
{
    @Autowired
    private IBatchHomeTutorialEntryService tutorialEntryService;

    /**
     * 查询首页教程入口列表
     */
    @PreAuthorize("@ss.hasPermi('batch:home:list')")
    @GetMapping("/list")
    public TableDataInfo list(BatchHomeTutorialEntry tutorialEntry)
    {
        startPage();
        List<BatchHomeTutorialEntry> list = tutorialEntryService.selectBatchHomeTutorialEntryList(tutorialEntry);
        return getDataTable(list);
    }

    /**
     * 导出首页教程入口列表
     */
    @PreAuthorize("@ss.hasPermi('batch:home:export')")
    @Log(title = "首页教程入口", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BatchHomeTutorialEntry tutorialEntry)
    {
        List<BatchHomeTutorialEntry> list = tutorialEntryService.selectBatchHomeTutorialEntryList(tutorialEntry);
        ExcelUtil<BatchHomeTutorialEntry> util = new ExcelUtil<BatchHomeTutorialEntry>(BatchHomeTutorialEntry.class);
        util.exportExcel(response, list, "首页教程入口数据");
    }

    /**
     * 获取首页教程入口详细信息
     */
    @PreAuthorize("@ss.hasPermi('batch:home:query')")
    @GetMapping(value = "/{entryId}")
    public AjaxResult getInfo(@PathVariable("entryId") Long entryId)
    {
        return success(tutorialEntryService.selectBatchHomeTutorialEntryById(entryId));
    }

    /**
     * 新增首页教程入口
     */
    @PreAuthorize("@ss.hasPermi('batch:home:add')")
    @Log(title = "首页教程入口", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BatchHomeTutorialEntry tutorialEntry)
    {
        tutorialEntry.setCreateBy(getUsername());
        return toAjax(tutorialEntryService.insertBatchHomeTutorialEntry(tutorialEntry));
    }

    /**
     * 修改首页教程入口
     */
    @PreAuthorize("@ss.hasPermi('batch:home:edit')")
    @Log(title = "首页教程入口", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BatchHomeTutorialEntry tutorialEntry)
    {
        tutorialEntry.setUpdateBy(getUsername());
        return toAjax(tutorialEntryService.updateBatchHomeTutorialEntry(tutorialEntry));
    }

    /**
     * 修改首页教程入口状态
     */
    @PreAuthorize("@ss.hasPermi('batch:home:edit')")
    @Log(title = "首页教程入口", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody BatchHomeTutorialEntry tutorialEntry)
    {
        BatchHomeTutorialEntry exist = tutorialEntryService.selectBatchHomeTutorialEntryById(tutorialEntry.getEntryId());
        if (exist == null)
        {
            return error("数据不存在");
        }
        exist.setStatus(tutorialEntry.getStatus());
        exist.setUpdateBy(getUsername());
        return toAjax(tutorialEntryService.updateBatchHomeTutorialEntry(exist));
    }

    /**
     * 删除首页教程入口
     */
    @PreAuthorize("@ss.hasPermi('batch:home:remove')")
    @Log(title = "首页教程入口", businessType = BusinessType.DELETE)
    @DeleteMapping("/{entryIds}")
    public AjaxResult remove(@PathVariable Long[] entryIds)
    {
        tutorialEntryService.deleteBatchHomeTutorialEntryByIds(entryIds);
        return success();
    }

    /**
     * 查询关联文档下拉列表
     */
    @PreAuthorize("@ss.hasPermi('batch:home:query')")
    @GetMapping("/documentList")
    public AjaxResult documentList()
    {
        List<BatchHomeDocumentOption> list = tutorialEntryService.selectDocumentOptionList();
        return success(list);
    }
}
