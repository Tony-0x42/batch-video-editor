package com.ruoyi.batch.computing.controller;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @Log(title = "算力消耗日志", businessType = BusinessType.OTHER)
    @GetMapping("/list")
    public TableDataInfo list(BatchComputingPowerLog log)
    {
        startPage();
        List<BatchComputingPowerLog> list = computingPowerLogService.selectList(log);
        return getDataTable(list);
    }

    /**
     * APP 查询当前登录账号的算力消耗日志（分页）
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @Log(title = "算力消耗日志", businessType = BusinessType.OTHER)
    @GetMapping("/my")
    public TableDataInfo my(BatchComputingPowerLog log)
    {
        log.setPhone(getUsername());
        startPage();
        List<BatchComputingPowerLog> list = computingPowerLogService.selectList(log);
        return getDataTable(list);
    }

    /**
     * 消耗算力（APP 端下载/生成前调用）
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @Log(title = "算力消耗", businessType = BusinessType.UPDATE)
    @PostMapping("/consume")
    public AjaxResult consume(@RequestBody ComputingConsumeBody body)
    {
        if (body == null || body.getOperationType() == null || body.getConsumeValue() == null)
        {
            return AjaxResult.error("参数错误");
        }
        BigDecimal remain = computingPowerLogService.consumeComputingPower(
                getUsername(), body.getOperationType(), body.getConsumeValue(), body.getBizNo());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("remain", remain);
        return ajax;
    }

    /**
     * 算力消耗请求体
     */
    public static class ComputingConsumeBody
    {
        /** 操作类型：1 生成 / 2 下载 */
        private Integer operationType;

        /** 消耗算力值 */
        private BigDecimal consumeValue;

        /** 业务单号/说明 */
        private String bizNo;

        public Integer getOperationType()
        {
            return operationType;
        }

        public void setOperationType(Integer operationType)
        {
            this.operationType = operationType;
        }

        public BigDecimal getConsumeValue()
        {
            return consumeValue;
        }

        public void setConsumeValue(BigDecimal consumeValue)
        {
            this.consumeValue = consumeValue;
        }

        public String getBizNo()
        {
            return bizNo;
        }

        public void setBizNo(String bizNo)
        {
            this.bizNo = bizNo;
        }
    }

}
