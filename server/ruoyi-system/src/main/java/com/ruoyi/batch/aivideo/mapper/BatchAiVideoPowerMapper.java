package com.ruoyi.batch.aivideo.mapper;

import java.math.BigDecimal;
import org.apache.ibatis.annotations.Param;

/**
 * AI 视频生成算力操作Mapper（原子扣减外的退款与日志落库，避免跨模块依赖）
 */
public interface BatchAiVideoPowerMapper
{
    /**
     * 退回算力（余额加回）
     *
     * @param phone 账号手机号
     * @param value 退回值
     * @return 影响行数
     */
    public int refundComputingPower(@Param("phone") String phone, @Param("value") BigDecimal value);

    /**
     * 写算力日志（remark 写入 video_group_name 列，跟随现有日志用法）
     *
     * @param phone 账号手机号
     * @param operationType 操作类型：1 生成 / 2 下载
     * @param consumeValue 消耗值（退款时为负数）
     * @param remainValue 操作后剩余算力
     * @param remark 说明
     * @return 影响行数
     */
    public int insertPowerLog(@Param("phone") String phone, @Param("operationType") Integer operationType,
            @Param("consumeValue") BigDecimal consumeValue, @Param("remainValue") BigDecimal remainValue,
            @Param("remark") String remark);
}
