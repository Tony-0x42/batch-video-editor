package com.ruoyi.batch.statistics.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.batch.statistics.domain.BatchAccountStat;
import com.ruoyi.batch.statistics.domain.BatchAccountTypePie;
import com.ruoyi.batch.statistics.domain.BatchComputingStat;
import com.ruoyi.batch.statistics.domain.BatchNewsStat;
import com.ruoyi.batch.statistics.domain.BatchQrCodePromotionStat;
import com.ruoyi.batch.statistics.domain.BatchStatisticsOverview;
import com.ruoyi.batch.statistics.domain.BatchStatisticsQuery;
import com.ruoyi.batch.statistics.domain.BatchTrendData;
import com.ruoyi.batch.statistics.domain.BatchVideoGenerateStat;
import com.ruoyi.batch.statistics.mapper.BatchStatisticsMapper;
import com.ruoyi.batch.statistics.service.IBatchStatisticsService;

/**
 * 数据统计 服务层实现
 *
 * @author ruoyi
 */
@Service
public class BatchStatisticsServiceImpl implements IBatchStatisticsService
{
    @Autowired
    private BatchStatisticsMapper statisticsMapper;

    /**
     * 应用数据权限：非超级管理员按当前登录手机号作为分公司手机号过滤
     */
    private void applyDataScope(BatchStatisticsQuery query)
    {
        if (!SecurityUtils.isAdmin())
        {
            String phone = SecurityUtils.getLoginUser().getUser().getPhonenumber();
            query.setBranchPhone(phone);
        }
    }

    /**
     * 处理概览指标空值
     */
    private void fillOverviewDefault(BatchStatisticsOverview overview)
    {
        if (overview == null)
        {
            overview = new BatchStatisticsOverview();
        }
        if (overview.getBranchTotal() == null)
        {
            overview.setBranchTotal(0L);
        }
        if (overview.getProviderTotal() == null)
        {
            overview.setProviderTotal(0L);
        }
        if (overview.getIndividualTotal() == null)
        {
            overview.setIndividualTotal(0L);
        }
        if (overview.getAccountTotal() == null)
        {
            overview.setAccountTotal(0L);
        }
        if (overview.getAccountTodayNew() == null)
        {
            overview.setAccountTodayNew(0L);
        }
        if (overview.getComputingTodayConsume() == null)
        {
            overview.setComputingTodayConsume(BigDecimal.ZERO);
        }
        if (overview.getVideoTodayGenerate() == null)
        {
            overview.setVideoTodayGenerate(0L);
        }
        if (overview.getQrScanToday() == null)
        {
            overview.setQrScanToday(0L);
        }
        if (overview.getQrDownloadToday() == null)
        {
            overview.setQrDownloadToday(0L);
        }
        if (overview.getQrRegisterToday() == null)
        {
            overview.setQrRegisterToday(0L);
        }
        if (overview.getNewsSalesAmount() == null)
        {
            overview.setNewsSalesAmount(BigDecimal.ZERO);
        }
        if (overview.getBranchServiceProviderCount() == null)
        {
            overview.setBranchServiceProviderCount(0L);
        }
        if (overview.getBranchIndividualCount() == null)
        {
            overview.setBranchIndividualCount(0L);
        }
        if (overview.getBranchServiceProviderRemain() == null)
        {
            overview.setBranchServiceProviderRemain(0L);
        }
        if (overview.getBranchMaxServiceProviderRemain() == null)
        {
            overview.setBranchMaxServiceProviderRemain(0L);
        }
    }

    @Override
    public BatchStatisticsOverview selectOverview(BatchStatisticsQuery query)
    {
        applyDataScope(query);
        BatchStatisticsOverview overview = statisticsMapper.selectOverview(query);
        if (overview == null)
        {
            overview = new BatchStatisticsOverview();
        }
        fillOverviewDefault(overview);
        return overview;
    }

    @Override
    public List<BatchAccountStat> selectAccountList(BatchStatisticsQuery query)
    {
        applyDataScope(query);
        return statisticsMapper.selectAccountList(query);
    }

    @Override
    public List<BatchComputingStat> selectComputingList(BatchStatisticsQuery query)
    {
        applyDataScope(query);
        return statisticsMapper.selectComputingList(query);
    }

    @Override
    public List<BatchVideoGenerateStat> selectVideoList(BatchStatisticsQuery query)
    {
        applyDataScope(query);
        return statisticsMapper.selectVideoList(query);
    }

    @Override
    public List<BatchQrCodePromotionStat> selectQrCodeList(BatchStatisticsQuery query)
    {
        applyDataScope(query);
        return statisticsMapper.selectQrCodeList(query);
    }

    @Override
    public List<BatchNewsStat> selectNewsList(BatchStatisticsQuery query)
    {
        applyDataScope(query);
        return statisticsMapper.selectNewsList(query);
    }

    @Override
    public BatchTrendData selectTrend(BatchStatisticsQuery query)
    {
        applyDataScope(query);
        int days = query.getDays() != null && query.getDays() > 0 ? query.getDays() : 7;
        Date endDate = DateUtils.getNowDate();
        Date startDate = DateUtils.addDays(endDate, -(days - 1));
        // 重置为当天开始与结束，避免时间部分干扰
        startDate = DateUtils.dateTime(DateUtils.YYYY_MM_DD, DateUtils.dateTime(startDate));
        endDate = DateUtils.dateTime(DateUtils.YYYY_MM_DD, DateUtils.dateTime(endDate));
        query.setStartDate(startDate);
        query.setEndDate(endDate);

        List<String> dateList = new ArrayList<String>();
        for (int i = days - 1; i >= 0; i--)
        {
            Date date = DateUtils.addDays(endDate, -i);
            dateList.add(DateUtils.dateTime(date));
        }

        List<Map<String, Object>> accountTrendList = statisticsMapper.selectAccountTrend(query);
        List<Map<String, Object>> computingTrendList = statisticsMapper.selectComputingTrend(query);
        List<Map<String, Object>> videoTrendList = statisticsMapper.selectVideoTrend(query);
        List<Map<String, Object>> qrScanTrendList = statisticsMapper.selectQrScanTrend(query);
        List<BatchAccountTypePie> accountTypePieList = statisticsMapper.selectAccountTypePie(query);

        Map<String, Long> accountMap = convertTrendToMap(accountTrendList);
        Map<String, BigDecimal> computingMap = convertTrendToDecimalMap(computingTrendList);
        Map<String, Long> videoMap = convertTrendToMap(videoTrendList);
        Map<String, Long> qrScanMap = convertTrendToMap(qrScanTrendList);

        BatchTrendData trendData = new BatchTrendData();
        trendData.setDates(dateList);
        List<Long> accountTrend = new ArrayList<Long>();
        List<BigDecimal> computingTrend = new ArrayList<BigDecimal>();
        List<Long> videoTrend = new ArrayList<Long>();
        List<Long> qrScanTrend = new ArrayList<Long>();

        for (String date : dateList)
        {
            accountTrend.add(accountMap.containsKey(date) ? accountMap.get(date) : 0L);
            computingTrend.add(computingMap.containsKey(date) ? computingMap.get(date) : BigDecimal.ZERO);
            videoTrend.add(videoMap.containsKey(date) ? videoMap.get(date) : 0L);
            qrScanTrend.add(qrScanMap.containsKey(date) ? qrScanMap.get(date) : 0L);
        }

        trendData.setAccountTrend(accountTrend);
        trendData.setComputingTrend(computingTrend);
        trendData.setVideoTrend(videoTrend);
        trendData.setQrScanTrend(qrScanTrend);
        trendData.setAccountTypePie(accountTypePieList != null ? accountTypePieList : new ArrayList<BatchAccountTypePie>());
        return trendData;
    }

    /**
     * 将趋势查询结果转换为日期-数量映射
     */
    private Map<String, Long> convertTrendToMap(List<Map<String, Object>> list)
    {
        Map<String, Long> map = new HashMap<String, Long>();
        if (list == null)
        {
            return map;
        }
        for (Map<String, Object> item : list)
        {
            String date = item.get("stat_date") != null ? item.get("stat_date").toString() : "";
            Long value = 0L;
            Object totalObj = item.get("total");
            if (totalObj != null)
            {
                if (totalObj instanceof Number)
                {
                    value = ((Number) totalObj).longValue();
                }
                else
                {
                    String str = totalObj.toString();
                    if (StringUtils.isNotEmpty(str))
                    {
                        value = Long.valueOf(str);
                    }
                }
            }
            if (StringUtils.isNotEmpty(date))
            {
                map.put(date, value);
            }
        }
        return map;
    }

    /**
     * 将趋势查询结果转换为日期-数值映射（BigDecimal）
     */
    private Map<String, BigDecimal> convertTrendToDecimalMap(List<Map<String, Object>> list)
    {
        Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
        if (list == null)
        {
            return map;
        }
        for (Map<String, Object> item : list)
        {
            String date = item.get("stat_date") != null ? item.get("stat_date").toString() : "";
            BigDecimal value = BigDecimal.ZERO;
            Object totalObj = item.get("total");
            if (totalObj != null)
            {
                if (totalObj instanceof BigDecimal)
                {
                    value = (BigDecimal) totalObj;
                }
                else if (totalObj instanceof Number)
                {
                    value = BigDecimal.valueOf(((Number) totalObj).doubleValue());
                }
                else
                {
                    String str = totalObj.toString();
                    if (StringUtils.isNotEmpty(str))
                    {
                        value = new BigDecimal(str);
                    }
                }
            }
            if (StringUtils.isNotEmpty(date))
            {
                map.put(date, value);
            }
        }
        return map;
    }
}
