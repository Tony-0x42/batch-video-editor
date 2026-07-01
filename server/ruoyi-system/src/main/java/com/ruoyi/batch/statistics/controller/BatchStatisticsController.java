package com.ruoyi.batch.statistics.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.batch.statistics.domain.BatchAccountStat;
import com.ruoyi.batch.statistics.domain.BatchComputingStat;
import com.ruoyi.batch.statistics.domain.BatchNewsStat;
import com.ruoyi.batch.statistics.domain.BatchQrCodePromotionStat;
import com.ruoyi.batch.statistics.domain.BatchStatisticsQuery;
import com.ruoyi.batch.statistics.domain.BatchTrendData;
import com.ruoyi.batch.statistics.domain.BatchVideoGenerateStat;
import com.ruoyi.batch.statistics.service.IBatchStatisticsService;

/**
 * 数据统计 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/batch/statistics")
public class BatchStatisticsController extends BaseController
{
    @Autowired
    private IBatchStatisticsService statisticsService;

    /**
     * 今日概览指标
     */
    @PreAuthorize("@ss.hasPermi('batch:statistics:query')")
    @GetMapping("/overview")
    public AjaxResult overview(BatchStatisticsQuery query)
    {
        return AjaxResult.success(statisticsService.selectOverview(query));
    }

    /**
     * 账号数据明细列表
     */
    @PreAuthorize("@ss.hasPermi('batch:statistics:query')")
    @GetMapping("/account")
    public TableDataInfo account(BatchStatisticsQuery query)
    {
        startPage();
        List<BatchAccountStat> list = statisticsService.selectAccountList(query);
        return getDataTable(list);
    }

    /**
     * 导出账号数据明细
     */
    @PreAuthorize("@ss.hasPermi('batch:statistics:export')")
    @Log(title = "数据统计-账号数据", businessType = BusinessType.EXPORT)
    @GetMapping("/account/export")
    public void exportAccount(HttpServletResponse response, BatchStatisticsQuery query)
    {
        List<BatchAccountStat> list = statisticsService.selectAccountList(query);
        ExcelUtil<BatchAccountStat> util = new ExcelUtil<BatchAccountStat>(BatchAccountStat.class);
        util.exportExcel(response, list, "账号数据");
    }

    /**
     * 算力消耗明细列表
     */
    @PreAuthorize("@ss.hasPermi('batch:statistics:query')")
    @GetMapping("/computing")
    public TableDataInfo computing(BatchStatisticsQuery query)
    {
        startPage();
        List<BatchComputingStat> list = statisticsService.selectComputingList(query);
        return getDataTable(list);
    }

    /**
     * 导出算力消耗明细
     */
    @PreAuthorize("@ss.hasPermi('batch:statistics:export')")
    @Log(title = "数据统计-算力消耗", businessType = BusinessType.EXPORT)
    @GetMapping("/computing/export")
    public void exportComputing(HttpServletResponse response, BatchStatisticsQuery query)
    {
        List<BatchComputingStat> list = statisticsService.selectComputingList(query);
        ExcelUtil<BatchComputingStat> util = new ExcelUtil<BatchComputingStat>(BatchComputingStat.class);
        util.exportExcel(response, list, "算力消耗数据");
    }

    /**
     * 视频生成明细列表
     */
    @PreAuthorize("@ss.hasPermi('batch:statistics:query')")
    @GetMapping("/video")
    public TableDataInfo video(BatchStatisticsQuery query)
    {
        startPage();
        List<BatchVideoGenerateStat> list = statisticsService.selectVideoList(query);
        return getDataTable(list);
    }

    /**
     * 导出视频生成明细
     */
    @PreAuthorize("@ss.hasPermi('batch:statistics:export')")
    @Log(title = "数据统计-视频生成", businessType = BusinessType.EXPORT)
    @GetMapping("/video/export")
    public void exportVideo(HttpServletResponse response, BatchStatisticsQuery query)
    {
        List<BatchVideoGenerateStat> list = statisticsService.selectVideoList(query);
        ExcelUtil<BatchVideoGenerateStat> util = new ExcelUtil<BatchVideoGenerateStat>(BatchVideoGenerateStat.class);
        util.exportExcel(response, list, "视频生成数据");
    }

    /**
     * 二维码推广明细列表
     */
    @PreAuthorize("@ss.hasPermi('batch:statistics:query')")
    @GetMapping("/qrcode")
    public TableDataInfo qrcode(BatchStatisticsQuery query)
    {
        startPage();
        List<BatchQrCodePromotionStat> list = statisticsService.selectQrCodeList(query);
        return getDataTable(list);
    }

    /**
     * 导出二维码推广明细
     */
    @PreAuthorize("@ss.hasPermi('batch:statistics:export')")
    @Log(title = "数据统计-二维码推广", businessType = BusinessType.EXPORT)
    @GetMapping("/qrcode/export")
    public void exportQrCode(HttpServletResponse response, BatchStatisticsQuery query)
    {
        List<BatchQrCodePromotionStat> list = statisticsService.selectQrCodeList(query);
        ExcelUtil<BatchQrCodePromotionStat> util = new ExcelUtil<BatchQrCodePromotionStat>(BatchQrCodePromotionStat.class);
        util.exportExcel(response, list, "二维码推广数据");
    }

    /**
     * 业绩喜报明细列表
     */
    @PreAuthorize("@ss.hasPermi('batch:statistics:query')")
    @GetMapping("/news")
    public TableDataInfo news(BatchStatisticsQuery query)
    {
        startPage();
        List<BatchNewsStat> list = statisticsService.selectNewsList(query);
        return getDataTable(list);
    }

    /**
     * 导出业绩喜报明细
     */
    @PreAuthorize("@ss.hasPermi('batch:statistics:export')")
    @Log(title = "数据统计-业绩喜报", businessType = BusinessType.EXPORT)
    @GetMapping("/news/export")
    public void exportNews(HttpServletResponse response, BatchStatisticsQuery query)
    {
        List<BatchNewsStat> list = statisticsService.selectNewsList(query);
        ExcelUtil<BatchNewsStat> util = new ExcelUtil<BatchNewsStat>(BatchNewsStat.class);
        util.exportExcel(response, list, "业绩喜报数据");
    }

    /**
     * 趋势数据
     */
    @PreAuthorize("@ss.hasPermi('batch:statistics:query')")
    @GetMapping("/trend")
    public AjaxResult trend(BatchStatisticsQuery query)
    {
        BatchTrendData trendData = statisticsService.selectTrend(query);
        return AjaxResult.success(trendData);
    }
}
