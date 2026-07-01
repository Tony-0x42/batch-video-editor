package com.ruoyi.batch.customer.domain;

import java.math.BigDecimal;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 客户/APP账号主表 batch_customer
 *
 * @author ruoyi
 */
public class BatchCustomer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 客户ID */
    private Long customerId;

    /** 账号类型：1 分公司 / 2 服务商 / 3 个人 */
    @NotNull(message = "账号类型不能为空")
    @Excel(name = "账号类型", readConverterExp = "1=分公司,2=服务商,3=个人")
    private Integer customerType;

    /** 账号名称 */
    @NotBlank(message = "账号名称不能为空")
    @Size(min = 2, max = 100, message = "账号名称长度必须介于 2 和 100 之间")
    @Excel(name = "账号名称")
    private String customerName;

    /** 联系人 */
    @NotBlank(message = "联系人不能为空")
    @Size(min = 2, max = 50, message = "联系人长度必须介于 2 和 50 之间")
    @Excel(name = "联系人")
    private String contactName;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Excel(name = "手机号")
    private String phone;

    /** 上级手机号 */
    @Excel(name = "上级手机号")
    private String parentPhone;

    /** 所属分公司手机号 */
    @Excel(name = "所属分公司手机号")
    private String branchPhone;

    /** 分公司最大可创建服务商数量 */
    private Integer maxServiceProvider;

    /** 分公司旗下服务商总可分配个人账号容量 */
    private Integer totalIndividualCapacity;

    /** 服务商可拆分创建个人账号上限 */
    private Integer maxIndividual;

    /** 算力总配额 GF */
    @NotNull(message = "算力总配额不能为空")
    @Excel(name = "算力总配额")
    private BigDecimal computingPowerTotal;

    /** 已消耗算力 GF */
    @Excel(name = "已消耗算力")
    private BigDecimal computingPowerUsed;

    /** 剩余算力 GF */
    @Excel(name = "剩余算力")
    private BigDecimal computingPowerRemain;

    /** VIP 有效期 */
    @NotNull(message = "VIP有效期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "VIP有效期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date vipExpireDate;

    /** 注册二维码图片 URL */
    private String qrCodeUrl;

    /** 二维码唯一 key */
    private String qrCodeKey;

    /** 状态：0 启用 / 1 禁用 */
    @Excel(name = "状态", readConverterExp = "0=启用,1=禁用")
    private Integer status;

    /** 删除标志：0 存在 / 2 删除 */
    private Integer delFlag;

    /** 下级数量（非表字段，用于列表展示） */
    private Integer subordinateCount;

    /** 已创建下级数量（非表字段，用于配额计算） */
    private Integer usedSubordinateCount;

    /** VIP 状态筛选：0有效 1已过期（非持久化字段） */
    private Integer vipStatus;

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public Integer getCustomerType()
    {
        return customerType;
    }

    public void setCustomerType(Integer customerType)
    {
        this.customerType = customerType;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public String getContactName()
    {
        return contactName;
    }

    public void setContactName(String contactName)
    {
        this.contactName = contactName;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getParentPhone()
    {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone)
    {
        this.parentPhone = parentPhone;
    }

    public String getBranchPhone()
    {
        return branchPhone;
    }

    public void setBranchPhone(String branchPhone)
    {
        this.branchPhone = branchPhone;
    }

    public Integer getMaxServiceProvider()
    {
        return maxServiceProvider;
    }

    public void setMaxServiceProvider(Integer maxServiceProvider)
    {
        this.maxServiceProvider = maxServiceProvider;
    }

    public Integer getTotalIndividualCapacity()
    {
        return totalIndividualCapacity;
    }

    public void setTotalIndividualCapacity(Integer totalIndividualCapacity)
    {
        this.totalIndividualCapacity = totalIndividualCapacity;
    }

    public Integer getMaxIndividual()
    {
        return maxIndividual;
    }

    public void setMaxIndividual(Integer maxIndividual)
    {
        this.maxIndividual = maxIndividual;
    }

    public BigDecimal getComputingPowerTotal()
    {
        return computingPowerTotal;
    }

    public void setComputingPowerTotal(BigDecimal computingPowerTotal)
    {
        this.computingPowerTotal = computingPowerTotal;
    }

    public BigDecimal getComputingPowerUsed()
    {
        return computingPowerUsed;
    }

    public void setComputingPowerUsed(BigDecimal computingPowerUsed)
    {
        this.computingPowerUsed = computingPowerUsed;
    }

    public BigDecimal getComputingPowerRemain()
    {
        return computingPowerRemain;
    }

    public void setComputingPowerRemain(BigDecimal computingPowerRemain)
    {
        this.computingPowerRemain = computingPowerRemain;
    }

    public Date getVipExpireDate()
    {
        return vipExpireDate;
    }

    public void setVipExpireDate(Date vipExpireDate)
    {
        this.vipExpireDate = vipExpireDate;
    }

    public String getQrCodeUrl()
    {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl)
    {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getQrCodeKey()
    {
        return qrCodeKey;
    }

    public void setQrCodeKey(String qrCodeKey)
    {
        this.qrCodeKey = qrCodeKey;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag)
    {
        this.delFlag = delFlag;
    }

    public Integer getSubordinateCount()
    {
        return subordinateCount;
    }

    public void setSubordinateCount(Integer subordinateCount)
    {
        this.subordinateCount = subordinateCount;
    }

    public Integer getUsedSubordinateCount()
    {
        return usedSubordinateCount;
    }

    public void setUsedSubordinateCount(Integer usedSubordinateCount)
    {
        this.usedSubordinateCount = usedSubordinateCount;
    }

    public Integer getVipStatus()
    {
        return vipStatus;
    }

    public void setVipStatus(Integer vipStatus)
    {
        this.vipStatus = vipStatus;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("customerId", getCustomerId())
            .append("customerType", getCustomerType())
            .append("customerName", getCustomerName())
            .append("contactName", getContactName())
            .append("phone", getPhone())
            .append("parentPhone", getParentPhone())
            .append("branchPhone", getBranchPhone())
            .append("maxServiceProvider", getMaxServiceProvider())
            .append("totalIndividualCapacity", getTotalIndividualCapacity())
            .append("maxIndividual", getMaxIndividual())
            .append("computingPowerTotal", getComputingPowerTotal())
            .append("computingPowerUsed", getComputingPowerUsed())
            .append("computingPowerRemain", getComputingPowerRemain())
            .append("vipExpireDate", getVipExpireDate())
            .append("qrCodeUrl", getQrCodeUrl())
            .append("qrCodeKey", getQrCodeKey())
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
