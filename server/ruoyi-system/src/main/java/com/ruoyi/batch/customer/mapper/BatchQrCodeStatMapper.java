package com.ruoyi.batch.customer.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 二维码推广统计Mapper接口（batch_qr_code_stat）
 *
 * @author ruoyi
 */
public interface BatchQrCodeStatMapper
{
    /**
     * 扫码次数+1（当日无记录则插入）
     *
     * @param phone 二维码所属账号手机号
     * @return 影响行数
     */
    public int incrementScanCount(@Param("phone") String phone);

    /**
     * 下载次数+1（当日无记录则插入）
     *
     * @param phone 二维码所属账号手机号
     * @return 影响行数
     */
    public int incrementDownloadCount(@Param("phone") String phone);

    /**
     * 注册次数+1（当日无记录则插入）
     *
     * @param phone 二维码所属账号手机号
     * @return 影响行数
     */
    public int incrementRegisterCount(@Param("phone") String phone);

    /**
     * 查询某手机号二维码推广累计统计
     *
     * @param phone 二维码所属账号手机号
     * @return 含 scanCount/downloadCount/registerCount 的 Map
     */
    public java.util.Map<String, Object> selectTotalsByPhone(@Param("phone") String phone);
}
