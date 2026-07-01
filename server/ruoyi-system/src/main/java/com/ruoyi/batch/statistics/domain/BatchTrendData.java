package com.ruoyi.batch.statistics.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 趋势数据
 *
 * @author ruoyi
 */
public class BatchTrendData implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 日期列表 */
    private List<String> dates = new ArrayList<String>();

    /** 新增账号趋势 */
    private List<Long> accountTrend = new ArrayList<Long>();

    /** 算力消耗趋势 */
    private List<BigDecimal> computingTrend = new ArrayList<BigDecimal>();

    /** 视频生成趋势 */
    private List<Long> videoTrend = new ArrayList<Long>();

    /** 二维码扫码趋势 */
    private List<Long> qrScanTrend = new ArrayList<Long>();

    /** 账号类型分布 */
    private List<BatchAccountTypePie> accountTypePie = new ArrayList<BatchAccountTypePie>();

    public List<String> getDates()
    {
        return dates;
    }

    public void setDates(List<String> dates)
    {
        this.dates = dates;
    }

    public List<Long> getAccountTrend()
    {
        return accountTrend;
    }

    public void setAccountTrend(List<Long> accountTrend)
    {
        this.accountTrend = accountTrend;
    }

    public List<BigDecimal> getComputingTrend()
    {
        return computingTrend;
    }

    public void setComputingTrend(List<BigDecimal> computingTrend)
    {
        this.computingTrend = computingTrend;
    }

    public List<Long> getVideoTrend()
    {
        return videoTrend;
    }

    public void setVideoTrend(List<Long> videoTrend)
    {
        this.videoTrend = videoTrend;
    }

    public List<Long> getQrScanTrend()
    {
        return qrScanTrend;
    }

    public void setQrScanTrend(List<Long> qrScanTrend)
    {
        this.qrScanTrend = qrScanTrend;
    }

    public List<BatchAccountTypePie> getAccountTypePie()
    {
        return accountTypePie;
    }

    public void setAccountTypePie(List<BatchAccountTypePie> accountTypePie)
    {
        this.accountTypePie = accountTypePie;
    }
}
