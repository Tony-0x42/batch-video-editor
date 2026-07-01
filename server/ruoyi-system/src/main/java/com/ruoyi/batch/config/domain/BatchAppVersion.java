package com.ruoyi.batch.config.domain;

import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * APP 版本管理表 batch_app_version
 *
 * @author ruoyi
 */
public class BatchAppVersion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 版本ID */
    @Excel(name = "版本ID", cellType = ColumnType.NUMERIC)
    private Long versionId;

    /** 版本号 */
    @Excel(name = "版本号")
    private String versionNo;

    /** 平台：1 Android / 2 iOS */
    @Excel(name = "平台", readConverterExp = "1=Android,2=iOS")
    private Integer platform;

    /** 更新类型：1 强制 / 2 提示 / 3 静默 */
    @Excel(name = "更新类型", readConverterExp = "1=强制,2=提示,3=静默")
    private Integer updateType;

    /** 更新内容 */
    private String updateContent;

    /** 下载地址 */
    private String downloadUrl;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发布时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /** 状态：0 启用 / 1 禁用 */
    @Excel(name = "状态", readConverterExp = "0=启用,1=禁用")
    private Integer status;

    public Long getVersionId()
    {
        return versionId;
    }

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    @NotBlank(message = "版本号不能为空")
    @Size(min = 0, max = 50, message = "版本号长度不能超过50个字符")
    public String getVersionNo()
    {
        return versionNo;
    }

    public void setVersionNo(String versionNo)
    {
        this.versionNo = versionNo;
    }

    public Integer getPlatform()
    {
        return platform;
    }

    public void setPlatform(Integer platform)
    {
        this.platform = platform;
    }

    public Integer getUpdateType()
    {
        return updateType;
    }

    public void setUpdateType(Integer updateType)
    {
        this.updateType = updateType;
    }

    public String getUpdateContent()
    {
        return updateContent;
    }

    public void setUpdateContent(String updateContent)
    {
        this.updateContent = updateContent;
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }

    public Date getPublishTime()
    {
        return publishTime;
    }

    public void setPublishTime(Date publishTime)
    {
        this.publishTime = publishTime;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("versionId", getVersionId())
            .append("versionNo", getVersionNo())
            .append("platform", getPlatform())
            .append("updateType", getUpdateType())
            .append("updateContent", getUpdateContent())
            .append("downloadUrl", getDownloadUrl())
            .append("publishTime", getPublishTime())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
