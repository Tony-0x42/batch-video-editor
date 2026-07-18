package com.ruoyi.batch.aivideo.domain;

import java.util.List;
import jakarta.validation.constraints.NotNull;

/**
 * AI 视频生成请求体
 */
public class BatchAiVideoGenerateBody
{
    /** 视频组ID */
    @NotNull(message = "视频组ID不能为空")
    private Long groupId;

    /** 本次生成数量（默认1，上限10） */
    private Integer count;

    /** 本次生成消耗算力 */
    private Integer consumeValue;

    /** 分镜头列表（可选，生成时以当前提交为准） */
    private List<BatchAiVideoClip> clips;

    public Long getGroupId()
    {
        return groupId;
    }

    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public Integer getCount()
    {
        return count;
    }

    public void setCount(Integer count)
    {
        this.count = count;
    }

    public Integer getConsumeValue()
    {
        return consumeValue;
    }

    public void setConsumeValue(Integer consumeValue)
    {
        this.consumeValue = consumeValue;
    }

    public List<BatchAiVideoClip> getClips()
    {
        return clips;
    }

    public void setClips(List<BatchAiVideoClip> clips)
    {
        this.clips = clips;
    }
}
