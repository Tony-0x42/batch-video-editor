package com.ruoyi.batch.home.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 首页教程入口对象 batch_home_tutorial_entry
 *
 * @author ruoyi
 */
public class BatchHomeTutorialEntry extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 入口ID */
    private Long entryId;

    /** 入口标题 */
    @Excel(name = "入口标题")
    private String title;

    /** 封面图URL */
    @Excel(name = "封面图URL")
    private String coverUrl;

    /** 关联文档ID */
    @Excel(name = "关联文档ID")
    private Long documentId;

    /** 关联文档标题（非数据库字段） */
    private String documentTitle;

    /** 排序权重 */
    @Excel(name = "排序权重")
    private Integer sortWeight;

    /** 状态：0 启用 / 1 禁用 */
    @Excel(name = "状态", readConverterExp = "0=启用,1=禁用")
    private String status;

    /** 删除标志：0 存在 / 2 删除 */
    private String delFlag;

    public Long getEntryId()
    {
        return entryId;
    }

    public void setEntryId(Long entryId)
    {
        this.entryId = entryId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getCoverUrl()
    {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl)
    {
        this.coverUrl = coverUrl;
    }

    public Long getDocumentId()
    {
        return documentId;
    }

    public void setDocumentId(Long documentId)
    {
        this.documentId = documentId;
    }

    public String getDocumentTitle()
    {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle)
    {
        this.documentTitle = documentTitle;
    }

    public Integer getSortWeight()
    {
        return sortWeight;
    }

    public void setSortWeight(Integer sortWeight)
    {
        this.sortWeight = sortWeight;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("entryId", getEntryId())
            .append("title", getTitle())
            .append("coverUrl", getCoverUrl())
            .append("documentId", getDocumentId())
            .append("documentTitle", getDocumentTitle())
            .append("sortWeight", getSortWeight())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
