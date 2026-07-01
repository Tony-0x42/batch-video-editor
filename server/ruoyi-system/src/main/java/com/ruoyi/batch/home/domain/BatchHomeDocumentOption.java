package com.ruoyi.batch.home.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 首页教程入口关联文档选项
 *
 * @author ruoyi
 */
public class BatchHomeDocumentOption
{
    /** 文档ID */
    private Long documentId;

    /** 文档标题 */
    private String documentTitle;

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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("documentId", getDocumentId())
            .append("documentTitle", getDocumentTitle())
            .toString();
    }
}
