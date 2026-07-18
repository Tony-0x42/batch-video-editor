package com.ruoyi.batch.watermark.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import com.ruoyi.batch.watermark.domain.BatchWatermarkParse;
import com.ruoyi.batch.watermark.service.IBatchWatermarkParseService;

/**
 * AI 去水印解析记录 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/batch/watermark/parse")
public class BatchWatermarkParseController extends BaseController
{
    @Autowired
    private IBatchWatermarkParseService batchWatermarkParseService;

    /**
     * 查询当前账号解析记录列表
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @Log(title = "AI去水印解析记录", businessType = BusinessType.OTHER)
    @GetMapping("/list")
    public TableDataInfo list(BatchWatermarkParse batchWatermarkParse)
    {
        String phone = SecurityUtils.getLoginUser().getUsername();
        batchWatermarkParse.setPhone(phone);
        startPage();
        List<BatchWatermarkParse> list = batchWatermarkParseService.selectBatchWatermarkParseList(batchWatermarkParse);
        return getDataTable(list);
    }

    /**
     * 根据 ID 获取解析详情（仅本人记录可查）
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @Log(title = "AI去水印解析记录", businessType = BusinessType.OTHER)
    @GetMapping("/{parseId}")
    public AjaxResult getInfo(@PathVariable("parseId") Long parseId)
    {
        BatchWatermarkParse parse = batchWatermarkParseService.selectBatchWatermarkParseById(parseId);
        if (parse == null)
        {
            return AjaxResult.error("解析记录不存在");
        }
        String phone = SecurityUtils.getLoginUser().getUsername();
        if (!phone.equals(parse.getPhone()))
        {
            return AjaxResult.error("无权查看该解析记录");
        }
        return AjaxResult.success(parse);
    }

    /**
     * 解析视频链接
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @Log(title = "AI去水印解析", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult parse(@RequestBody BatchWatermarkParse parse)
    {
        try
        {
            String phone = SecurityUtils.getLoginUser().getUsername();
            String sourceLink = parse.getSourceLink();
            if (StringUtils.isEmpty(sourceLink))
            {
                return AjaxResult.error("请输入分享链接");
            }
            BatchWatermarkParse result = batchWatermarkParseService.parseLink(phone, sourceLink);
            if (result == null)
            {
                return AjaxResult.error("解析失败，请稍后重试");
            }
            if (Integer.valueOf(9).equals(result.getParseStatus()))
            {
                return AjaxResult.error(StringUtils.isNotEmpty(result.getFailReason())
                        ? result.getFailReason() : "解析失败，请检查链接是否有效");
            }
            return AjaxResult.success(result);
        }
        catch (Exception e)
        {
            return AjaxResult.error("解析服务异常，请稍后重试");
        }
    }

    /**
     * 删除解析记录（兼容旧的 GET 方式）
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @Log(title = "AI去水印解析记录", businessType = BusinessType.DELETE)
    @GetMapping("/delete/{parseIds}")
    public AjaxResult remove(@PathVariable Long[] parseIds)
    {
        return toAjax(batchWatermarkParseService.deleteBatchWatermarkParseByIds(parseIds));
    }

    /**
     * 删除解析记录
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @Log(title = "AI去水印解析记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{parseIds}")
    public AjaxResult removeByDelete(@PathVariable Long[] parseIds)
    {
        return toAjax(batchWatermarkParseService.deleteBatchWatermarkParseByIds(parseIds));
    }
}
