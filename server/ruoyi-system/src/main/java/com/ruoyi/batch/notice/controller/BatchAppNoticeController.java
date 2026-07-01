package com.ruoyi.batch.notice.controller;

import java.util.List;
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
import com.ruoyi.batch.notice.domain.BatchAppNotice;
import com.ruoyi.batch.notice.service.IBatchAppNoticeService;

/**
 * APP 公告 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/batch/notice")
public class BatchAppNoticeController extends BaseController
{
    @Autowired
    private IBatchAppNoticeService batchAppNoticeService;

    /**
     * 查询APP公告列表
     */
    @PreAuthorize("@ss.hasPermi('batch:notice:list')")
    @GetMapping("/list")
    public TableDataInfo list(BatchAppNotice batchAppNotice)
    {
        startPage();
        List<BatchAppNotice> list = batchAppNoticeService.selectBatchAppNoticeList(batchAppNotice);
        return getDataTable(list);
    }

    /**
     * 导出APP公告列表
     */
    @PreAuthorize("@ss.hasPermi('batch:notice:export')")
    @Log(title = "APP公告", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(BatchAppNotice batchAppNotice)
    {
        List<BatchAppNotice> list = batchAppNoticeService.selectBatchAppNoticeList(batchAppNotice);
        ExcelUtil<BatchAppNotice> util = new ExcelUtil<BatchAppNotice>(BatchAppNotice.class);
        return util.exportExcel(list, "APP公告数据");
    }

    /**
     * 获取APP公告详细信息
     */
    @PreAuthorize("@ss.hasPermi('batch:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable("noticeId") Long noticeId)
    {
        return AjaxResult.success(batchAppNoticeService.selectBatchAppNoticeById(noticeId));
    }

    /**
     * 预览APP公告
     */
    @PreAuthorize("@ss.hasPermi('batch:notice:query')")
    @GetMapping(value = "/preview/{noticeId}")
    public AjaxResult preview(@PathVariable("noticeId") Long noticeId)
    {
        return AjaxResult.success(batchAppNoticeService.selectBatchAppNoticeById(noticeId));
    }

    /**
     * 新增APP公告
     */
    @PreAuthorize("@ss.hasPermi('batch:notice:add')")
    @Log(title = "APP公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BatchAppNotice batchAppNotice)
    {
        if (batchAppNotice.getNoticeTitle() == null || batchAppNotice.getNoticeTitle().trim().isEmpty())
        {
            return AjaxResult.error("请填写公告标题");
        }
        if (batchAppNotice.getContent() == null || batchAppNotice.getContent().trim().isEmpty())
        {
            return AjaxResult.error("请填写公告内容");
        }
        batchAppNotice.setCreateBy(getUsername());
        return toAjax(batchAppNoticeService.insertBatchAppNotice(batchAppNotice));
    }

    /**
     * 修改APP公告
     */
    @PreAuthorize("@ss.hasPermi('batch:notice:edit')")
    @Log(title = "APP公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BatchAppNotice batchAppNotice)
    {
        if (batchAppNotice.getNoticeTitle() == null || batchAppNotice.getNoticeTitle().trim().isEmpty())
        {
            return AjaxResult.error("请填写公告标题");
        }
        if (batchAppNotice.getContent() == null || batchAppNotice.getContent().trim().isEmpty())
        {
            return AjaxResult.error("请填写公告内容");
        }
        batchAppNotice.setUpdateBy(getUsername());
        return toAjax(batchAppNoticeService.updateBatchAppNotice(batchAppNotice));
    }

    /**
     * 删除APP公告
     */
    @PreAuthorize("@ss.hasPermi('batch:notice:remove')")
    @Log(title = "APP公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds)
    {
        return toAjax(batchAppNoticeService.deleteBatchAppNoticeByIds(noticeIds));
    }

    /**
     * 发布公告
     */
    @PreAuthorize("@ss.hasPermi('batch:notice:publish')")
    @Log(title = "APP公告", businessType = BusinessType.UPDATE)
    @PutMapping("/publish/{noticeId}")
    public AjaxResult publish(@PathVariable("noticeId") Long noticeId)
    {
        return toAjax(batchAppNoticeService.publishNotice(noticeId));
    }

    /**
     * 下架公告
     */
    @PreAuthorize("@ss.hasPermi('batch:notice:edit')")
    @Log(title = "APP公告", businessType = BusinessType.UPDATE)
    @PutMapping("/unpublish/{noticeId}")
    public AjaxResult unpublish(@PathVariable("noticeId") Long noticeId)
    {
        return toAjax(batchAppNoticeService.unpublishNotice(noticeId));
    }
}
