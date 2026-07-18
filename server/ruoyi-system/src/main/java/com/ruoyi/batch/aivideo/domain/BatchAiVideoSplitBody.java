package com.ruoyi.batch.aivideo.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AI 视频分割请求体
 */
public class BatchAiVideoSplitBody
{
    /** 视频组ID */
    @NotNull(message = "视频组ID不能为空")
    private Long groupId;

    /** 已上传视频 URL（/profile/ 开头或完整 URL） */
    @NotBlank(message = "视频地址不能为空")
    private String videoUrl;

    /** 切片时长（秒），区间 0.5~10 */
    @NotNull(message = "切片时长不能为空")
    private Double sliceDuration;

    public Long getGroupId()
    {
        return groupId;
    }

    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public String getVideoUrl()
    {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl)
    {
        this.videoUrl = videoUrl;
    }

    public Double getSliceDuration()
    {
        return sliceDuration;
    }

    public void setSliceDuration(Double sliceDuration)
    {
        this.sliceDuration = sliceDuration;
    }
}
