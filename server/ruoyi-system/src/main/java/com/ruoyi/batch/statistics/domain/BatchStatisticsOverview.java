package com.ruoyi.batch.statistics.domain;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 数据统计今日概览指标
 *
 * @author ruoyi
 */
public class BatchStatisticsOverview implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 分公司总数 */
    private Long branchTotal = 0L;

    /** 服务商总数 */
    private Long providerTotal = 0L;

    /** 个人账号总数 */
    private Long individualTotal = 0L;

    /** 账号总数 */
    private Long accountTotal = 0L;

    /** 今日新增账号数 */
    private Long accountTodayNew = 0L;

    /** 今日算力消耗总量 */
    private BigDecimal computingTodayConsume = BigDecimal.ZERO;

    /** 今日视频生成数量 */
    private Long videoTodayGenerate = 0L;

    /** 今日二维码扫码次数 */
    private Long qrScanToday = 0L;

    /** 今日二维码下载量 */
    private Long qrDownloadToday = 0L;

    /** 今日二维码注册数 */
    private Long qrRegisterToday = 0L;

    /** 喜报业绩总金额 */
    private BigDecimal newsSalesAmount = BigDecimal.ZERO;

    /** 本公司服务商总数量（分公司后台） */
    private Long branchServiceProviderCount = 0L;

    /** 本公司个人账号总数（分公司后台） */
    private Long branchIndividualCount = 0L;

    /** 服务商剩余可创建个人账号名额（分公司后台） */
    private Long branchServiceProviderRemain = 0L;

    /** 分公司剩余可新增服务商名额（分公司后台） */
    private Long branchMaxServiceProviderRemain = 0L;

    public Long getBranchTotal()
    {
        return branchTotal;
    }

    public void setBranchTotal(Long branchTotal)
    {
        this.branchTotal = branchTotal;
    }

    public Long getProviderTotal()
    {
        return providerTotal;
    }

    public void setProviderTotal(Long providerTotal)
    {
        this.providerTotal = providerTotal;
    }

    public Long getIndividualTotal()
    {
        return individualTotal;
    }

    public void setIndividualTotal(Long individualTotal)
    {
        this.individualTotal = individualTotal;
    }

    public Long getAccountTotal()
    {
        return accountTotal;
    }

    public void setAccountTotal(Long accountTotal)
    {
        this.accountTotal = accountTotal;
    }

    public Long getAccountTodayNew()
    {
        return accountTodayNew;
    }

    public void setAccountTodayNew(Long accountTodayNew)
    {
        this.accountTodayNew = accountTodayNew;
    }

    public BigDecimal getComputingTodayConsume()
    {
        return computingTodayConsume;
    }

    public void setComputingTodayConsume(BigDecimal computingTodayConsume)
    {
        this.computingTodayConsume = computingTodayConsume;
    }

    public Long getVideoTodayGenerate()
    {
        return videoTodayGenerate;
    }

    public void setVideoTodayGenerate(Long videoTodayGenerate)
    {
        this.videoTodayGenerate = videoTodayGenerate;
    }

    public Long getQrScanToday()
    {
        return qrScanToday;
    }

    public void setQrScanToday(Long qrScanToday)
    {
        this.qrScanToday = qrScanToday;
    }

    public Long getQrDownloadToday()
    {
        return qrDownloadToday;
    }

    public void setQrDownloadToday(Long qrDownloadToday)
    {
        this.qrDownloadToday = qrDownloadToday;
    }

    public Long getQrRegisterToday()
    {
        return qrRegisterToday;
    }

    public void setQrRegisterToday(Long qrRegisterToday)
    {
        this.qrRegisterToday = qrRegisterToday;
    }

    public BigDecimal getNewsSalesAmount()
    {
        return newsSalesAmount;
    }

    public void setNewsSalesAmount(BigDecimal newsSalesAmount)
    {
        this.newsSalesAmount = newsSalesAmount;
    }

    public Long getBranchServiceProviderCount()
    {
        return branchServiceProviderCount;
    }

    public void setBranchServiceProviderCount(Long branchServiceProviderCount)
    {
        this.branchServiceProviderCount = branchServiceProviderCount;
    }

    public Long getBranchIndividualCount()
    {
        return branchIndividualCount;
    }

    public void setBranchIndividualCount(Long branchIndividualCount)
    {
        this.branchIndividualCount = branchIndividualCount;
    }

    public Long getBranchServiceProviderRemain()
    {
        return branchServiceProviderRemain;
    }

    public void setBranchServiceProviderRemain(Long branchServiceProviderRemain)
    {
        this.branchServiceProviderRemain = branchServiceProviderRemain;
    }

    public Long getBranchMaxServiceProviderRemain()
    {
        return branchMaxServiceProviderRemain;
    }

    public void setBranchMaxServiceProviderRemain(Long branchMaxServiceProviderRemain)
    {
        this.branchMaxServiceProviderRemain = branchMaxServiceProviderRemain;
    }
}
