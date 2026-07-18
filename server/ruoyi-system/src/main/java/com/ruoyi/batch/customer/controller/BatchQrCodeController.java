package com.ruoyi.batch.customer.controller;

import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.batch.customer.service.IBatchQrCodeStatService;

/**
 * 注册二维码扫码统计入口（匿名访问）
 * <p>
 * 二维码内容指向本接口：扫码后对应账号的扫码次数+1，然后 302 重定向到 APP 下载地址
 * （配置项 batch.app.download-url）。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/batch/qrcode")
public class BatchQrCodeController
{
    /** APP 下载地址 */
    @Value("${batch.app.download-url:https://batchvideo.example.com/download}")
    private String appDownloadUrl;

    @Autowired
    private IBatchQrCodeStatService qrCodeStatService;

    /**
     * 扫码统计并跳转 APP 下载地址
     */
    @Anonymous
    @GetMapping("/scan")
    public void scan(@RequestParam(value = "phone", required = false) String phone,
                     HttpServletResponse response) throws IOException
    {
        qrCodeStatService.incrementScanCount(phone);
        response.sendRedirect(appDownloadUrl);
    }
}
