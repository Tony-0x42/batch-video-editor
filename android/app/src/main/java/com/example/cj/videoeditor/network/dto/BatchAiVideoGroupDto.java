package com.example.cj.videoeditor.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * AI 云创视频组 DTO（对应后端 BatchAiVideoGroup）。
 */
public class BatchAiVideoGroupDto {

    @SerializedName("groupId")
    private Long groupId;

    @SerializedName("phone")
    private String phone;

    @SerializedName("groupName")
    private String groupName;

    @SerializedName("generatedCount")
    private Integer generatedCount;

    @SerializedName("maxLimit")
    private Integer maxLimit;

    @SerializedName("status")
    private Integer status;

    @SerializedName("createTime")
    private String createTime;

    @SerializedName("sortWeight")
    private Integer sortWeight;

    @SerializedName("clips")
    private List<BatchAiVideoClipDto> clips;

    public BatchAiVideoGroupDto() {
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getGeneratedCount() {
        return generatedCount;
    }

    public void setGeneratedCount(Integer generatedCount) {
        this.generatedCount = generatedCount;
    }

    public Integer getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(Integer maxLimit) {
        this.maxLimit = maxLimit;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Integer getSortWeight() {
        return sortWeight;
    }

    public void setSortWeight(Integer sortWeight) {
        this.sortWeight = sortWeight;
    }

    public List<BatchAiVideoClipDto> getClips() {
        return clips;
    }

    public void setClips(List<BatchAiVideoClipDto> clips) {
        this.clips = clips;
    }
}
