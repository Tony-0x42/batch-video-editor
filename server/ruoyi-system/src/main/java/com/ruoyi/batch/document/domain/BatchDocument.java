package com.ruoyi.batch.document.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 文档管理对象 batch_document
 *
 * @author ruoyi
 */
public class BatchDocument extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 文档ID */
    private Long documentId;

    /** 文档标题 */
    @Excel(name = "文档标题")
    private String documentTitle;

    /** 文档类型：1 用户协议 / 2 隐私政策 / 3 新手文档 / 4 帮助文档 */
    @Excel(name = "文档类型", readConverterExp = "1=用户协议,2=隐私政策,3=新手文档,4=帮助文档")
    private Integer documentType;

    /** 适用页面，逗号分隔 */
    @Excel(name = "适用页面")
    private String applyPages;

    /** 富文本内容 */
    private String content;

    /** 排序权重 */
    @Excel(name = "排序权重")
    private Integer sortWeight;

    /** 状态：0 启用 / 1 禁用 */
    @Excel(name = "状态", readConverterExp = "0=启用,1=禁用")
    private Integer status;

    /** 是否系统默认：1 系统默认不可删除 / 0 否 */
    private Integer isSystem;

    public Long getDocumentId()
    {
        return documentId;
    }

    public void setDocumentId(Long documentId)
    {
        this.documentId = documentId;
    }

    public void setDocumentTitle(String documentTitle)
    {
        this.documentTitle = documentTitle;
    }

    @NotBlank(message = "文档标题不能为空")
    @Size(min = 0, max = 200, message = "文档标题不能超过200个字符")
    public String getDocumentTitle()
    {
        return documentTitle;
    }

    public void setDocumentType(Integer documentType)
    {
        this.documentType = documentType;
    }

    @NotNull(message = "文档类型不能为空")
    public Integer getDocumentType()
    {
        return documentType;
    }

    public void setApplyPages(String applyPages)
    {
        this.applyPages = applyPages;
    }

    @Size(min = 0, max = 200, message = "适用页面不能超过200个字符")
    public String getApplyPages()
    {
        return applyPages;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    @NotBlank(message = "文档内容不能为空")
    public String getContent()
    {
        return content;
    }

    public void setSortWeight(Integer sortWeight)
    {
        this.sortWeight = sortWeight;
    }

    public Integer getSortWeight()
    {
        return sortWeight;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setIsSystem(Integer isSystem)
    {
        this.isSystem = isSystem;
    }

    public Integer getIsSystem()
    {
        return isSystem;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("documentId", getDocumentId())
            .append("documentTitle", getDocumentTitle())
            .append("documentType", getDocumentType())
            .append("applyPages", getApplyPages())
            .append("content", getContent())
            .append("sortWeight", getSortWeight())
            .append("status", getStatus())
            .append("isSystem", getIsSystem())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
