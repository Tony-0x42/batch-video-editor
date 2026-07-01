package com.ruoyi.batch.computing.controller;

import java.util.List;
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
import com.ruoyi.batch.computing.domain.BatchComputingPowerLog;
import com.ruoyi.batch.computing.service.IBatchComputingPowerLogService;

/**
 * 算力消耗日志 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/batch/computing/log")
public class BatchComputingPowerLogController extends BaseController
{
    @Autowired
    private IBatchComputingPowerLogService computingPowerLogService;

    /**
     * 查询算力消耗日志列表
     */
    @PreAuthorize("@ss.hasPermi('batch:customer:query')")
    @Log(title = "算力消耗日志", businessType = BusinessType.QUERY)
    @GetMapping("/list")
    public TableDataInfo list(BatchComputingPowerLog log)
    {
        startPage();
        List<BatchComputingPowerLog> list = computingPowerLogService.selectList(log);
        return getDataTable(list);
    }

}
