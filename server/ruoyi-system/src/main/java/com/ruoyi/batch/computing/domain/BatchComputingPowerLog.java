package com.ruoyi.batch.computing.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 算力消耗日志对象 batch_computing_power_log
 *
 * @author ruoyi
 */
public class BatchComputingPowerLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 账号手机号 */
    private String phone;

    /** 操作类型：1生成 2下载 */
    private Integer operationType;

    /** 消耗算力 */
    private BigDecimal consumeValue;

    /** 剩余算力 */
    private BigDecimal remainValue;

    /** 关联视频组名称 */
    private String videoGroupName;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public Integer getOperationType()
    {
        return operationType;
    }

    public void setOperationType(Integer operationType)
    {
        this.operationType = operationType;
    }

    public BigDecimal getConsumeValue()
    {
        return consumeValue;
    }

    public void setConsumeValue(BigDecimal consumeValue)
    {
        this.consumeValue = consumeValue;
    }

    public BigDecimal getRemainValue()
    {
        return remainValue;
    }

    public void setRemainValue(BigDecimal remainValue)
    {
        this.remainValue = remainValue;
    }

    public String getVideoGroupName()
    {
        return videoGroupName;
    }

    public void setVideoGroupName(String videoGroupName)
    {
        this.videoGroupName = videoGroupName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("phone", getPhone())
            .append("operationType", getOperationType())
            .append("consumeValue", getConsumeValue())
            .append("remainValue", getRemainValue())
            .append("videoGroupName", getVideoGroupName())
            .append("createTime", getCreateTime())
            .toString();
    }
}
