package com.ruoyi.batch.notice.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * APP 公告 batch_app_notice
 *
 * @author ruoyi
 */
public class BatchAppNotice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 公告ID */
    @Excel(name = "公告ID", sort = 1)
    private Long noticeId;

    /** 公告标题 */
    @Excel(name = "公告标题", sort = 2)
    private String noticeTitle;

    /** 公告类型（1通知 2活动 3重要更新） */
    @Excel(name = "公告类型", sort = 3, readConverterExp = "1=通知,2=活动,3=重要更新")
    private Integer noticeType;

    /** 封面图 URL */
    private String coverUrl;

    /** 富文本内容 */
    private String content;

    /** 发布状态（0已发布 1已下架 2暂存） */
    @Excel(name = "发布状态", sort = 4, readConverterExp = "0=已发布,1=已下架,2=暂存")
    private Integer publishStatus;

    /** 发布时间 */
    @Excel(name = "发布时间", sort = 5, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /** 阅读量 */
    @Excel(name = "阅读量", sort = 6)
    private Integer readCount;

    public Long getNoticeId()
    {
        return noticeId;
    }

    public void setNoticeId(Long noticeId)
    {
        this.noticeId = noticeId;
    }

    public String getNoticeTitle()
    {
        return noticeTitle;
    }

    public void setNoticeTitle(String noticeTitle)
    {
        this.noticeTitle = noticeTitle;
    }

    public Integer getNoticeType()
    {
        return noticeType;
    }

    public void setNoticeType(Integer noticeType)
    {
        this.noticeType = noticeType;
    }

    public String getCoverUrl()
    {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl)
    {
        this.coverUrl = coverUrl;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Integer getPublishStatus()
    {
        return publishStatus;
    }

    public void setPublishStatus(Integer publishStatus)
    {
        this.publishStatus = publishStatus;
    }

    public Date getPublishTime()
    {
        return publishTime;
    }

    public void setPublishTime(Date publishTime)
    {
        this.publishTime = publishTime;
    }

    public Integer getReadCount()
    {
        return readCount;
    }

    public void setReadCount(Integer readCount)
    {
        this.readCount = readCount;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("noticeId", getNoticeId())
            .append("noticeTitle", getNoticeTitle())
            .append("noticeType", getNoticeType())
            .append("coverUrl", getCoverUrl())
            .append("content", getContent())
            .append("publishStatus", getPublishStatus())
            .append("publishTime", getPublishTime())
            .append("readCount", getReadCount())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
