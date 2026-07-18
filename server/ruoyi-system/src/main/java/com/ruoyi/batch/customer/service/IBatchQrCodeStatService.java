package com.ruoyi.batch.customer.service;

/**
 * 二维码推广统计Service接口
 *
 * @author ruoyi
 */
public interface IBatchQrCodeStatService
{
    /**
     * 扫码次数+1
     *
     * @param phone 二维码所属账号手机号
     */
    public void incrementScanCount(String phone);

    /**
     * 下载次数+1
     *
     * @param phone 二维码所属账号手机号
     */
    public void incrementDownloadCount(String phone);

    /**
     * 注册次数+1
     *
     * @param phone 二维码所属账号手机号
     */
    public void incrementRegisterCount(String phone);

    /**
     * 查询某手机号二维码推广累计统计
     *
     * @param phone 二维码所属账号手机号
     * @return 含 scanCount/downloadCount/registerCount 的 Map（无记录时各项为 0）
     */
    public java.util.Map<String, Object> selectTotalsByPhone(String phone);
}
