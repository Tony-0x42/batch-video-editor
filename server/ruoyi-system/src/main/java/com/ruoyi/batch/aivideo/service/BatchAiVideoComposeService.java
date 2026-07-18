package com.ruoyi.batch.aivideo.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoClip;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoGenerateLog;
import com.ruoyi.batch.aivideo.ffmpeg.BatchFfmpegService;
import com.ruoyi.batch.aivideo.mapper.BatchAiVideoGenerateLogMapper;
import com.ruoyi.batch.aivideo.mapper.BatchAiVideoPowerMapper;
import com.ruoyi.batch.customer.domain.BatchCustomer;
import com.ruoyi.batch.customer.mapper.BatchCustomerMapper;

/**
 * AI 视频批量合成异步执行服务
 *
 * 注意：异步线程拿不到 RequestContext，登录用户手机号必须在提交任务时显式传入
 */
@Service
public class BatchAiVideoComposeService
{
    private static final Logger log = LoggerFactory.getLogger(BatchAiVideoComposeService.class);

    /** MD5 去重最大重试次数 */
    private static final int MAX_RETRY = 3;

    @Autowired
    private BatchFfmpegService ffmpegService;

    @Autowired
    private BatchAiVideoGenerateLogMapper generateLogMapper;

    @Autowired
    private BatchAiVideoPowerMapper powerMapper;

    @Autowired
    private BatchCustomerMapper customerMapper;

    @Autowired
    @Qualifier("batchAiVideoExecutor")
    private ExecutorService executor;

    /**
     * 提交一个批次的合成任务（每条 log 对应一个产出视频）
     *
     * @param phone    登录用户手机号（退款用）
     * @param groupId  视频组ID
     * @param groupName 视频组名称
     * @param clips    提交时刻的分镜头快照
     * @param logIds   本批次生成记录ID
     * @param perCost  单条产出消耗算力（失败时按此退回）
     */
    public void submitBatch(String phone, Long groupId, String groupName, List<BatchAiVideoClip> clips,
            List<Long> logIds, BigDecimal perCost)
    {
        executor.submit(() -> runBatch(phone, groupId, groupName, clips, logIds, perCost));
    }

    private void runBatch(String phone, Long groupId, String groupName, List<BatchAiVideoClip> clips,
            List<Long> logIds, BigDecimal perCost)
    {
        // 同批次产出 MD5 集合，用于产出间去重
        Set<String> md5Set = new HashSet<>();
        for (Long logId : logIds)
        {
            try
            {
                composeOne(phone, groupId, clips, logId, md5Set);
            }
            catch (Exception e)
            {
                log.error("AI 视频合成失败, logId={}", logId, e);
                failAndRefund(phone, groupName, logId, perCost, e.getMessage());
            }
        }
    }

    private void composeOne(String phone, Long groupId, List<BatchAiVideoClip> clips, Long logId, Set<String> md5Set)
            throws Exception
    {
        if (!ffmpegService.isAvailable())
        {
            throw new IllegalStateException("FFmpeg 不可用，请检查服务器 batch.video.ffmpeg-path 配置");
        }
        BatchAiVideoGenerateLog task = generateLogMapper.selectBatchAiVideoGenerateLogById(logId);
        if (task == null)
        {
            throw new IllegalStateException("生成记录不存在");
        }

        List<File> sources = new ArrayList<>();
        for (BatchAiVideoClip clip : clips)
        {
            sources.add(ffmpegService.resolveLocalFile(clip.getVideoUrl()));
        }

        File outDir = new File(RuoYiConfig.getProfile() + "/video/output/" + groupId);
        if (!outDir.exists() && !outDir.mkdirs())
        {
            throw new IllegalStateException("无法创建输出目录: " + outDir.getAbsolutePath());
        }
        long baseSeed = task.getClipSeed() != null ? task.getClipSeed().hashCode() : logId;
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++)
        {
            File outFile = new File(outDir, logId + "_" + attempt + ".mp4");
            long seed = baseSeed * 31L + attempt;
            String md5 = ffmpegService.compose(sources, outFile, seed,
                    progress -> updateProgress(logId, progress));
            if (md5Set.add(md5))
            {
                // 成功
                BatchAiVideoGenerateLog update = new BatchAiVideoGenerateLog();
                update.setLogId(logId);
                update.setStatus(1);
                update.setProgress(100);
                update.setResultUrl(ffmpegService.toUrl(outFile));
                generateLogMapper.updateBatchAiVideoGenerateLog(update);
                return;
            }
            log.info("产出 MD5 与本批次其他产出重复，换种子重试: logId={}, attempt={}", logId, attempt);
            outFile.delete();
            if (attempt == MAX_RETRY)
            {
                throw new IllegalStateException("多次重试后产出仍与其他视频重复");
            }
        }
    }

    private void updateProgress(Long logId, int progress)
    {
        try
        {
            BatchAiVideoGenerateLog update = new BatchAiVideoGenerateLog();
            update.setLogId(logId);
            update.setProgress(progress);
            generateLogMapper.updateBatchAiVideoGenerateLog(update);
        }
        catch (Exception e)
        {
            log.warn("进度更新失败: logId={}, progress={}", logId, progress);
        }
    }

    /**
     * 任务置为失败并退回该条对应算力（余额加回 + 写退款算力日志）
     */
    private void failAndRefund(String phone, String groupName, Long logId, BigDecimal perCost, String errorMsg)
    {
        BatchAiVideoGenerateLog update = new BatchAiVideoGenerateLog();
        update.setLogId(logId);
        update.setStatus(2);
        String msg = errorMsg == null ? "合成失败" : errorMsg;
        update.setErrorMsg(msg.length() > 500 ? msg.substring(0, 500) : msg);
        generateLogMapper.updateBatchAiVideoGenerateLog(update);
        try
        {
            powerMapper.refundComputingPower(phone, perCost);
            BigDecimal remain = queryRemain(phone);
            powerMapper.insertPowerLog(phone, 1, perCost.negate(), remain, "AI视频生成失败退款-" + groupName);
        }
        catch (Exception e)
        {
            log.error("算力退回失败: phone={}, logId={}", phone, logId, e);
        }
    }

    private BigDecimal queryRemain(String phone)
    {
        BatchCustomer customer = customerMapper.selectBatchCustomerByPhone(phone);
        if (customer != null && customer.getComputingPowerRemain() != null)
        {
            return customer.getComputingPowerRemain();
        }
        return BigDecimal.ZERO;
    }
}
