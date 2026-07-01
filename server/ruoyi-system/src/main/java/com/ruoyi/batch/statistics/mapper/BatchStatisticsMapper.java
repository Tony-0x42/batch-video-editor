package com.ruoyi.batch.statistics.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.batch.statistics.domain.BatchAccountStat;
import com.ruoyi.batch.statistics.domain.BatchAccountTypePie;
import com.ruoyi.batch.statistics.domain.BatchComputingStat;
import com.ruoyi.batch.statistics.domain.BatchNewsStat;
import com.ruoyi.batch.statistics.domain.BatchQrCodePromotionStat;
import com.ruoyi.batch.statistics.domain.BatchStatisticsOverview;
import com.ruoyi.batch.statistics.domain.BatchStatisticsQuery;
import com.ruoyi.batch.statistics.domain.BatchVideoGenerateStat;

/**
 * 数据统计 Mapper接口
 *
 * @author ruoyi
 */
public interface BatchStatisticsMapper
{
    /**
     * 查询今日概览指标
     *
     * @param query 查询条件
     * @return 概览指标
     */
    public BatchStatisticsOverview selectOverview(BatchStatisticsQuery query);

    /**
     * 查询账号数据明细列表
     *
     * @param query 查询条件
     * @return 账号数据明细列表
     */
    public List<BatchAccountStat> selectAccountList(BatchStatisticsQuery query);

    /**
     * 查询算力消耗明细列表
     *
     * @param query 查询条件
     * @return 算力消耗明细列表
     */
    public List<BatchComputingStat> selectComputingList(BatchStatisticsQuery query);

    /**
     * 查询视频生成明细列表
     *
     * @param query 查询条件
     * @return 视频生成明细列表
     */
    public List<BatchVideoGenerateStat> selectVideoList(BatchStatisticsQuery query);

    /**
     * 查询二维码推广明细列表
     *
     * @param query 查询条件
     * @return 二维码推广明细列表
     */
    public List<BatchQrCodePromotionStat> selectQrCodeList(BatchStatisticsQuery query);

    /**
     * 查询业绩喜报明细列表
     *
     * @param query 查询条件
     * @return 业绩喜报明细列表
     */
    public List<BatchNewsStat> selectNewsList(BatchStatisticsQuery query);

    /**
     * 查询新增账号趋势
     *
     * @param query 查询条件
     * @return 日期-数量映射列表
     */
    public List<Map<String, Object>> selectAccountTrend(BatchStatisticsQuery query);

    /**
     * 查询算力消耗趋势
     *
     * @param query 查询条件
     * @return 日期-数值映射列表
     */
    public List<Map<String, Object>> selectComputingTrend(BatchStatisticsQuery query);

    /**
     * 查询视频生成趋势
     *
     * @param query 查询条件
     * @return 日期-数量映射列表
     */
    public List<Map<String, Object>> selectVideoTrend(BatchStatisticsQuery query);

    /**
     * 查询二维码扫码趋势
     *
     * @param query 查询条件
     * @return 日期-数量映射列表
     */
    public List<Map<String, Object>> selectQrScanTrend(BatchStatisticsQuery query);

    /**
     * 查询账号类型分布
     *
     * @param query 查询条件
     * @return 账号类型分布列表
     */
    public List<BatchAccountTypePie> selectAccountTypePie(BatchStatisticsQuery query);
}
