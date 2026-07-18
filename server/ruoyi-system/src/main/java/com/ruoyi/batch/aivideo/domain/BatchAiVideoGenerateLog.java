package com.ruoyi.batch.aivideo.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI 视频生成记录对象（对应 batch_video_generate_log 表）
 *
 * status 语义：0 处理中 / 1 成功 / 2 失败
 */
public class BatchAiVideoGenerateLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 账号手机号 */
    private String phone;

    /** 所属视频组ID */
    private Long groupId;

    /** 视频组名称 */
    private String videoGroupName;

    /** 生成数量（每条记录对应一个产出视频，固定为1） */
    private Integer generateCount;

    /** 本条产出消耗算力 */
    private BigDecimal consumeValue;

    /** 状态：0 处理中 / 1 成功 / 2 失败 */
    private Integer status;

    /** 生成进度 0-100 */
    private Integer progress;

    /** 产出视频访问URL */
    private String resultUrl;

    /** 失败原因 */
    private String errorMsg;

    /** 随机化种子（去重策略用） */
    private String clipSeed;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public Long getGroupId()
    {
        return groupId;
    }

    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public String getVideoGroupName()
    {
        return videoGroupName;
    }

    public void setVideoGroupName(String videoGroupName)
    {
        this.videoGroupName = videoGroupName;
    }

    public Integer getGenerateCount()
    {
        return generateCount;
    }

    public void setGenerateCount(Integer generateCount)
    {
        this.generateCount = generateCount;
    }

    public BigDecimal getConsumeValue()
    {
        return consumeValue;
    }

    public void setConsumeValue(BigDecimal consumeValue)
    {
        this.consumeValue = consumeValue;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getProgress()
    {
        return progress;
    }

    public void setProgress(Integer progress)
    {
        this.progress = progress;
    }

    public String getResultUrl()
    {
        return resultUrl;
    }

    public void setResultUrl(String resultUrl)
    {
        this.resultUrl = resultUrl;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public String getClipSeed()
    {
        return clipSeed;
    }

    public void setClipSeed(String clipSeed)
    {
        this.clipSeed = clipSeed;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("phone", getPhone())
            .append("groupId", getGroupId())
            .append("videoGroupName", getVideoGroupName())
            .append("generateCount", getGenerateCount())
            .append("consumeValue", getConsumeValue())
            .append("status", getStatus())
            .append("progress", getProgress())
            .append("resultUrl", getResultUrl())
            .append("errorMsg", getErrorMsg())
            .append("clipSeed", getClipSeed())
            .append("createTime", getCreateTime())
            .toString();
    }
}
