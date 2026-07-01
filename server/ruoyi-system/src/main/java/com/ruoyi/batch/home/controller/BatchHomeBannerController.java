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
import com.ruoyi.batch.home.domain.BatchHomeBanner;
import com.ruoyi.batch.home.service.IBatchHomeBannerService;

/**
 * 首页轮播图Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/batch/home/banner")
public class BatchHomeBannerController extends BaseController
{
    @Autowired
    private IBatchHomeBannerService bannerService;

    /**
     * 查询首页轮播图列表
     */
    @PreAuthorize("@ss.hasPermi('batch:home:list')")
    @GetMapping("/list")
    public TableDataInfo list(BatchHomeBanner banner)
    {
        startPage();
        List<BatchHomeBanner> list = bannerService.selectBatchHomeBannerList(banner);
        return getDataTable(list);
    }

    /**
     * 导出首页轮播图列表
     */
    @PreAuthorize("@ss.hasPermi('batch:home:export')")
    @Log(title = "首页轮播图", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BatchHomeBanner banner)
    {
        List<BatchHomeBanner> list = bannerService.selectBatchHomeBannerList(banner);
        ExcelUtil<BatchHomeBanner> util = new ExcelUtil<BatchHomeBanner>(BatchHomeBanner.class);
        util.exportExcel(response, list, "首页轮播图数据");
    }

    /**
     * 获取首页轮播图详细信息
     */
    @PreAuthorize("@ss.hasPermi('batch:home:query')")
    @GetMapping(value = "/{bannerId}")
    public AjaxResult getInfo(@PathVariable("bannerId") Long bannerId)
    {
        return success(bannerService.selectBatchHomeBannerById(bannerId));
    }

    /**
     * 新增首页轮播图
     */
    @PreAuthorize("@ss.hasPermi('batch:home:add')")
    @Log(title = "首页轮播图", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BatchHomeBanner banner)
    {
        banner.setCreateBy(getUsername());
        return toAjax(bannerService.insertBatchHomeBanner(banner));
    }

    /**
     * 修改首页轮播图
     */
    @PreAuthorize("@ss.hasPermi('batch:home:edit')")
    @Log(title = "首页轮播图", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BatchHomeBanner banner)
    {
        banner.setUpdateBy(getUsername());
        return toAjax(bannerService.updateBatchHomeBanner(banner));
    }

    /**
     * 修改首页轮播图状态
     */
    @PreAuthorize("@ss.hasPermi('batch:home:edit')")
    @Log(title = "首页轮播图", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody BatchHomeBanner banner)
    {
        BatchHomeBanner exist = bannerService.selectBatchHomeBannerById(banner.getBannerId());
        if (exist == null)
        {
            return error("数据不存在");
        }
        exist.setStatus(banner.getStatus());
        exist.setUpdateBy(getUsername());
        return toAjax(bannerService.updateBatchHomeBanner(exist));
    }

    /**
     * 删除首页轮播图
     */
    @PreAuthorize("@ss.hasPermi('batch:home:remove')")
    @Log(title = "首页轮播图", businessType = BusinessType.DELETE)
    @DeleteMapping("/{bannerIds}")
    public AjaxResult remove(@PathVariable Long[] bannerIds)
    {
        bannerService.deleteBatchHomeBannerByIds(bannerIds);
        return success();
    }
}
