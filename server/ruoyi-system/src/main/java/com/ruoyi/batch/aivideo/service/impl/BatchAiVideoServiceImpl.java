package com.ruoyi.batch.aivideo.service.impl;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoClip;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoGenerateBody;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoGenerateLog;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoGroup;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoSplitBody;
import com.ruoyi.batch.aivideo.ffmpeg.BatchFfmpegService;
import com.ruoyi.batch.aivideo.mapper.BatchAiVideoClipMapper;
import com.ruoyi.batch.aivideo.mapper.BatchAiVideoGenerateLogMapper;
import com.ruoyi.batch.aivideo.mapper.BatchAiVideoGroupMapper;
import com.ruoyi.batch.aivideo.mapper.BatchAiVideoPowerMapper;
import com.ruoyi.batch.aivideo.service.IBatchAiVideoService;
import com.ruoyi.batch.aivideo.service.BatchAiVideoComposeService;
import com.ruoyi.batch.config.service.IBatchSystemConfigService;
import com.ruoyi.batch.customer.domain.BatchCustomer;
import com.ruoyi.batch.customer.mapper.BatchCustomerMapper;

/**
 * AI 云创视频服务实现
 */
@Service
public class BatchAiVideoServiceImpl implements IBatchAiVideoService
{
    /** 单次批量生成数量上限 */
    private static final int MAX_GENERATE_COUNT = 10;

    /** 单条产出默认算力成本（batch_system_config 中 batch.ai.generateCost 未配置时使用） */
    private static final BigDecimal DEFAULT_GENERATE_COST = BigDecimal.ONE;

    @Autowired
    private BatchAiVideoGroupMapper groupMapper;

    @Autowired
    private BatchAiVideoClipMapper clipMapper;

    @Autowired
    private BatchCustomerMapper customerMapper;

    @Autowired
    private BatchAiVideoGenerateLogMapper generateLogMapper;

    @Autowired
    private BatchAiVideoPowerMapper powerMapper;

    @Autowired
    private BatchFfmpegService ffmpegService;

    @Autowired
    private BatchAiVideoComposeService composeService;

    @Autowired
    private IBatchSystemConfigService systemConfigService;

    @Override
    public List<BatchAiVideoGroup> selectBatchAiVideoGroupList(String phone)
    {
        BatchAiVideoGroup query = new BatchAiVideoGroup();
        query.setPhone(phone);
        return groupMapper.selectBatchAiVideoGroupList(query);
    }

    @Override
    public BatchAiVideoGroup selectBatchAiVideoGroupById(Long groupId)
    {
        BatchAiVideoGroup group = groupMapper.selectBatchAiVideoGroupById(groupId);
        if (group != null)
        {
            group.setClips(clipMapper.selectBatchAiVideoClipByGroupId(groupId));
        }
        return group;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBatchAiVideoGroup(BatchAiVideoGroup group)
    {
        if (group.getGeneratedCount() == null)
        {
            group.setGeneratedCount(0);
        }
        if (group.getMaxLimit() == null)
        {
            group.setMaxLimit(10);
        }
        if (group.getStatus() == null)
        {
            group.setStatus(0);
        }
        if (group.getDelFlag() == null)
        {
            group.setDelFlag(0);
        }
        group.setCreateTime(DateUtils.getNowDate());
        return groupMapper.insertBatchAiVideoGroup(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBatchAiVideoGroup(BatchAiVideoGroup group)
    {
        group.setUpdateTime(DateUtils.getNowDate());
        int rows = groupMapper.updateBatchAiVideoGroup(group);
        if (rows > 0 && group.getClips() != null)
        {
            saveClips(group.getGroupId(), group.getClips());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatchAiVideoGroupById(Long groupId)
    {
        clipMapper.deleteBatchAiVideoClipByGroupId(groupId);
        return groupMapper.deleteBatchAiVideoGroupById(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchAiVideoClip> splitVideo(String phone, BatchAiVideoSplitBody body)
    {
        Long groupId = body.getGroupId();
        BatchAiVideoGroup group = groupMapper.selectBatchAiVideoGroupById(groupId);
        checkGroupOwner(group, phone);

        double sliceDuration = body.getSliceDuration();
        double sliceMin = getConfigDouble("batch.ai.sliceMin", 0.5);
        double sliceMax = getConfigDouble("batch.ai.sliceMax", 10.0);
        if (sliceDuration < sliceMin || sliceDuration > sliceMax)
        {
            throw new ServiceException("切片时长需在 " + sliceMin + "~" + sliceMax + " 秒之间");
        }
        if (!ffmpegService.isAvailable())
        {
            throw new ServiceException("视频处理服务暂不可用，请联系管理员");
        }

        File input = ffmpegService.resolveLocalFile(body.getVideoUrl());
        File outDir = new File(RuoYiConfig.getProfile() + "/video/split/" + groupId + "/" + System.currentTimeMillis());
        List<BatchFfmpegService.Segment> segments;
        try
        {
            segments = ffmpegService.split(input, sliceDuration, outDir);
        }
        catch (Exception e)
        {
            throw new ServiceException("视频分割失败: " + e.getMessage());
        }

        // 切段结果覆盖为该组最新分镜头
        clipMapper.deleteBatchAiVideoClipByGroupId(groupId);
        List<BatchAiVideoClip> clips = new ArrayList<>();
        int order = 0;
        for (BatchFfmpegService.Segment segment : segments)
        {
            BatchAiVideoClip clip = new BatchAiVideoClip();
            clip.setGroupId(groupId);
            clip.setVideoUrl(ffmpegService.toUrl(segment.getFile()));
            clip.setDuration(segment.getDuration());
            clip.setSortOrder(order++);
            clips.add(clip);
        }
        if (!clips.isEmpty())
        {
            clipMapper.insertBatchAiVideoClipBatch(clips);
        }
        return clips;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> submitGenerate(String phone, BatchAiVideoGenerateBody body)
    {
        Long groupId = body.getGroupId();
        BatchAiVideoGroup group = groupMapper.selectBatchAiVideoGroupById(groupId);
        checkGroupOwner(group, phone);

        // 生成数量：默认1，上限10
        int count = body.getCount() == null || body.getCount() < 1 ? 1 : body.getCount();
        if (count > MAX_GENERATE_COUNT)
        {
            throw new ServiceException("单次最多生成 " + MAX_GENERATE_COUNT + " 个视频");
        }

        // 保存最新分镜头（clips 为 null 时不动的约定保留）
        if (body.getClips() != null)
        {
            saveClips(groupId, body.getClips());
        }
        List<BatchAiVideoClip> clips = clipMapper.selectBatchAiVideoClipByGroupId(groupId);
        if (clips.isEmpty())
        {
            throw new ServiceException("请先上传素材并完成 AI 分割后再生成");
        }

        // 算力总消耗 = count × 单条成本
        BatchCustomer customer = customerMapper.selectBatchCustomerByPhone(phone);
        if (customer == null)
        {
            throw new ServiceException("账号信息异常");
        }
        BigDecimal perCost = getGenerateCost();
        BigDecimal totalCost = perCost.multiply(new BigDecimal(count));
        BigDecimal remain = customer.getComputingPowerRemain() != null ? customer.getComputingPowerRemain() : BigDecimal.ZERO;
        if (remain.compareTo(totalCost) < 0)
        {
            throw new ServiceException("当前算力已耗尽，请联系管理员增加算力额度");
        }

        // 带余额条件的原子扣减，并发下不会扣成负数
        int rows = customerMapper.consumeComputingPower(customer.getCustomerId(), totalCost);
        if (rows <= 0)
        {
            throw new ServiceException("当前算力已耗尽，请联系管理员增加算力额度");
        }
        powerMapper.insertPowerLog(phone, 1, totalCost, remain.subtract(totalCost),
                "AI视频生成x" + count + "-" + group.getGroupName());

        // 增加已生成次数
        groupMapper.incrementGeneratedCount(groupId, count);

        // 每个产出视频一行生成记录：status=0 处理中，独立随机种子
        List<Long> logIds = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            BatchAiVideoGenerateLog task = new BatchAiVideoGenerateLog();
            task.setPhone(phone);
            task.setGroupId(groupId);
            task.setVideoGroupName(group.getGroupName());
            task.setGenerateCount(1);
            task.setConsumeValue(perCost);
            task.setStatus(0);
            task.setProgress(0);
            task.setClipSeed(UUID.randomUUID().toString());
            task.setCreateTime(DateUtils.getNowDate());
            generateLogMapper.insertBatchAiVideoGenerateLog(task);
            logIds.add(task.getLogId());
        }

        // 异步执行真实合成（异步线程无 RequestContext，手机号显式传入）
        composeService.submitBatch(phone, groupId, group.getGroupName(), clips, logIds, perCost);
        return logIds;
    }

    @Override
    public List<BatchAiVideoGenerateLog> selectBatchAiVideoGenerateLogList(BatchAiVideoGenerateLog query)
    {
        return generateLogMapper.selectBatchAiVideoGenerateLogList(query);
    }

    @Override
    public BatchAiVideoGenerateLog selectBatchAiVideoGenerateLogById(Long logId)
    {
        return generateLogMapper.selectBatchAiVideoGenerateLogById(logId);
    }

    /**
     * 校验视频组归属（APP 用户只能操作自己的组）
     */
    private void checkGroupOwner(BatchAiVideoGroup group, String phone)
    {
        if (group == null)
        {
            throw new ServiceException("视频组不存在");
        }
        if (!phone.equals(group.getPhone()))
        {
            throw new ServiceException("无权操作该视频组");
        }
    }

    /**
     * 覆盖保存分镜头（批量 insert）
     */
    private void saveClips(Long groupId, List<BatchAiVideoClip> clips)
    {
        clipMapper.deleteBatchAiVideoClipByGroupId(groupId);
        if (clips == null || clips.isEmpty())
        {
            return;
        }
        int order = 0;
        for (BatchAiVideoClip clip : clips)
        {
            clip.setGroupId(groupId);
            clip.setSortOrder(order++);
        }
        clipMapper.insertBatchAiVideoClipBatch(clips);
    }

    /**
     * 单条产出算力成本（读全局参数 batch.ai.generateCost，未配置默认 1）
     */
    private BigDecimal getGenerateCost()
    {
        try
        {
            String value = systemConfigService.selectConfigValueByKey("batch.ai.generateCost");
            if (StringUtils.isNotEmpty(value))
            {
                BigDecimal cost = new BigDecimal(value.trim());
                if (cost.compareTo(BigDecimal.ZERO) > 0)
                {
                    return cost;
                }
            }
        }
        catch (Exception e)
        {
            // 配置缺失或格式异常时按默认成本处理
        }
        return DEFAULT_GENERATE_COST;
    }

    private double getConfigDouble(String key, double defaultValue)
    {
        try
        {
            String value = systemConfigService.selectConfigValueByKey(key);
            if (StringUtils.isNotEmpty(value))
            {
                return Double.parseDouble(value.trim());
            }
        }
        catch (Exception e)
        {
            // 配置缺失或格式异常时按默认值处理
        }
        return defaultValue;
    }
}
