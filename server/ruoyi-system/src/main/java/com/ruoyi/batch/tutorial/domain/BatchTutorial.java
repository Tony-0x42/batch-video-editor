package com.ruoyi.batch.tutorial.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 教程表 batch_tutorial
 * 
 * @author ruoyi
 */
public class BatchTutorial extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 教程ID */
    private Long tutorialId;

    /** 教程标题 */
    @Excel(name = "教程标题")
    private String tutorialTitle;

    /** 教程类型（1视频 2图文） */
    @Excel(name = "教程类型", readConverterExp = "1=视频,2=图文")
    private String tutorialType;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称（非持久化，列表展示） */
    private String categoryName;

    /** 封面图URL */
    private String coverUrl;

    /** 视频文件URL */
    private String videoUrl;

    /** 图文内容 */
    private String documentContent;

    /** 简介 */
    private String intro;

    /** 排序权重 */
    @Excel(name = "排序权重")
    private Integer sortWeight;

    /** 浏览次数 */
    @Excel(name = "浏览次数", cellType = Excel.ColumnType.NUMERIC)
    private Integer viewCount;

    /** 状态（0上架 1下架） */
    @Excel(name = "状态", readConverterExp = "0=上架,1=下架")
    private String status;

    public Long getTutorialId()
    {
        return tutorialId;
    }

    public void setTutorialId(Long tutorialId)
    {
        this.tutorialId = tutorialId;
    }

    @NotBlank(message = "教程标题不能为空")
    @Size(min = 0, max = 200, message = "教程标题不能超过200个字符")
    public String getTutorialTitle()
    {
        return tutorialTitle;
    }

    public void setTutorialTitle(String tutorialTitle)
    {
        this.tutorialTitle = tutorialTitle;
    }

    @NotBlank(message = "教程类型不能为空")
    public String getTutorialType()
    {
        return tutorialType;
    }

    public void setTutorialType(String tutorialType)
    {
        this.tutorialType = tutorialType;
    }

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public String getCoverUrl()
    {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl)
    {
        this.coverUrl = coverUrl;
    }

    public String getVideoUrl()
    {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl)
    {
        this.videoUrl = videoUrl;
    }

    public String getDocumentContent()
    {
        return documentContent;
    }

    public void setDocumentContent(String documentContent)
    {
        this.documentContent = documentContent;
    }

    @Size(min = 0, max = 500, message = "简介不能超过500个字符")
    public String getIntro()
    {
        return intro;
    }

    public void setIntro(String intro)
    {
        this.intro = intro;
    }

    public Integer getSortWeight()
    {
        return sortWeight;
    }

    public void setSortWeight(Integer sortWeight)
    {
        this.sortWeight = sortWeight;
    }

    public Integer getViewCount()
    {
        return viewCount;
    }

    public void setViewCount(Integer viewCount)
    {
        this.viewCount = viewCount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("tutorialId", getTutorialId())
            .append("tutorialTitle", getTutorialTitle())
            .append("tutorialType", getTutorialType())
            .append("categoryId", getCategoryId())
            .append("coverUrl", getCoverUrl())
            .append("videoUrl", getVideoUrl())
            .append("documentContent", getDocumentContent())
            .append("intro", getIntro())
            .append("sortWeight", getSortWeight())
            .append("viewCount", getViewCount())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
