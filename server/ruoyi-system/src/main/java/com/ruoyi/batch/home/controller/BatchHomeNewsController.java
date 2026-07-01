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
import com.ruoyi.batch.home.domain.BatchHomeNews;
import com.ruoyi.batch.home.service.IBatchHomeNewsService;

/**
 * 首页喜报数据Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/batch/home/news")
public class BatchHomeNewsController extends BaseController
{
    @Autowired
    private IBatchHomeNewsService newsService;

    /**
     * 查询首页喜报数据列表
     */
    @PreAuthorize("@ss.hasPermi('batch:home:list')")
    @GetMapping("/list")
    public TableDataInfo list(BatchHomeNews news)
    {
        startPage();
        List<BatchHomeNews> list = newsService.selectBatchHomeNewsList(news);
        return getDataTable(list);
    }

    /**
     * 导出首页喜报数据列表
     */
    @PreAuthorize("@ss.hasPermi('batch:home:export')")
    @Log(title = "首页喜报数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BatchHomeNews news)
    {
        List<BatchHomeNews> list = newsService.selectBatchHomeNewsList(news);
        ExcelUtil<BatchHomeNews> util = new ExcelUtil<BatchHomeNews>(BatchHomeNews.class);
        util.exportExcel(response, list, "首页喜报数据");
    }

    /**
     * 获取首页喜报数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('batch:home:query')")
    @GetMapping(value = "/{newsId}")
    public AjaxResult getInfo(@PathVariable("newsId") Long newsId)
    {
        return success(newsService.selectBatchHomeNewsById(newsId));
    }

    /**
     * 新增首页喜报数据
     */
    @PreAuthorize("@ss.hasPermi('batch:home:add')")
    @Log(title = "首页喜报数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BatchHomeNews news)
    {
        news.setCreateBy(getUsername());
        return toAjax(newsService.insertBatchHomeNews(news));
    }

    /**
     * 修改首页喜报数据
     */
    @PreAuthorize("@ss.hasPermi('batch:home:edit')")
    @Log(title = "首页喜报数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BatchHomeNews news)
    {
        news.setUpdateBy(getUsername());
        return toAjax(newsService.updateBatchHomeNews(news));
    }

    /**
     * 修改首页喜报数据状态
     */
    @PreAuthorize("@ss.hasPermi('batch:home:edit')")
    @Log(title = "首页喜报数据", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody BatchHomeNews news)
    {
        BatchHomeNews exist = newsService.selectBatchHomeNewsById(news.getNewsId());
        if (exist == null)
        {
            return error("数据不存在");
        }
        exist.setStatus(news.getStatus());
        exist.setUpdateBy(getUsername());
        return toAjax(newsService.updateBatchHomeNews(exist));
    }

    /**
     * 删除首页喜报数据
     */
    @PreAuthorize("@ss.hasPermi('batch:home:remove')")
    @Log(title = "首页喜报数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{newsIds}")
    public AjaxResult remove(@PathVariable Long[] newsIds)
    {
        newsService.deleteBatchHomeNewsByIds(newsIds);
        return success();
    }
}
