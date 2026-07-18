package com.ruoyi.batch.aivideo.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 视频生成相关 Bean 配置
 */
@Configuration
public class BatchAiVideoTaskConfig
{
    /**
     * AI 视频合成线程池（异步线程拿不到 RequestContext，登录用户信息需在提交时显式传入）
     */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService batchAiVideoExecutor()
    {
        AtomicInteger index = new AtomicInteger(1);
        return Executors.newFixedThreadPool(2, r ->
        {
            Thread thread = new Thread(r, "batch-ai-video-" + index.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 视频上传体积较大，放宽 multipart 限制（覆盖 application.yml 中默认的 10MB/20MB）
     */
    @Bean
    public MultipartConfigElement multipartConfigElement()
    {
        long maxFileSize = 500L * 1024 * 1024;
        long maxRequestSize = 520L * 1024 * 1024;
        return new MultipartConfigElement("", maxFileSize, maxRequestSize, (int) maxFileSize);
    }
}
