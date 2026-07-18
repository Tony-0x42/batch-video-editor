package com.ruoyi.batch.customer.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.batch.customer.domain.BatchCustomer;

/**
 * 客户/APP账号Mapper接口
 *
 * @author ruoyi
 */
public interface BatchCustomerMapper
{
    /**
     * 查询客户列表
     *
     * @param batchCustomer 查询条件
     * @return 客户列表
     */
    public List<BatchCustomer> selectBatchCustomerList(BatchCustomer batchCustomer);

    /**
     * 根据ID查询客户
     *
     * @param customerId 客户ID
     * @return 客户信息
     */
    public BatchCustomer selectBatchCustomerById(Long customerId);

    /**
     * 根据手机号查询客户
     *
     * @param phone 手机号
     * @return 客户信息
     */
    public BatchCustomer selectBatchCustomerByPhone(String phone);

    /**
     * 新增客户
     *
     * @param batchCustomer 客户信息
     * @return 影响行数
     */
    public int insertBatchCustomer(BatchCustomer batchCustomer);

    /**
     * 修改客户
     *
     * @param batchCustomer 客户信息
     * @return 影响行数
     */
    public int updateBatchCustomer(BatchCustomer batchCustomer);

    /**
     * 批量删除客户
     *
     * @param customerIds 客户ID数组
     * @return 影响行数
     */
    public int deleteBatchCustomerByIds(Long[] customerIds);

    /**
     * 按上级手机号统计下级数量
     *
     * @param parentPhone 上级手机号
     * @return 下级数量
     */
    public int countByParentPhone(String parentPhone);

    /**
     * 按所属分公司手机号统计数量
     *
     * @param branchPhone 分公司手机号
     * @return 数量
     */
    public int countByBranchPhone(String branchPhone);

    /**
     * 按上级手机号查询下级列表
     *
     * @param parentPhone 上级手机号
     * @return 下级列表
     */
    public List<BatchCustomer> selectByParentPhone(String parentPhone);

    /**
     * 校验手机号是否唯一
     *
     * @param phone 手机号
     * @param excludeId 排除的客户ID
     * @return 数量
     */
    public int checkPhoneUnique(@Param("phone") String phone, @Param("excludeId") Long excludeId);

    /**
     * 更新客户二维码信息
     *
     * @param batchCustomer 客户信息
     * @return 影响行数
     */
    public int updateQrCode(BatchCustomer batchCustomer);

    /**
     * 修改VIP有效期
     *
     * @param customerId 客户ID
     * @param vipExpireDate VIP有效期
     * @return 影响行数
     */
    public int updateVipExpireDate(@Param("customerId") Long customerId, @Param("vipExpireDate") java.util.Date vipExpireDate);

    /**
     * 批量修改VIP有效期
     *
     * @param customerIds 客户ID数组
     * @param vipExpireDate VIP有效期
     * @return 影响行数
     */
    public int updateVipExpireDateBatch(@Param("customerIds") Long[] customerIds, @Param("vipExpireDate") java.util.Date vipExpireDate);

    /**
     * 扣减算力
     *
     * @param customerId 客户ID
     * @param value 扣减值
     * @return 影响行数
     */
    public int consumeComputingPower(@Param("customerId") Long customerId, @Param("value") java.math.BigDecimal value);

    /**
     * 按手机号原子扣减算力（余额不足时影响行数为 0）
     *
     * @param phone 手机号
     * @param value 扣减值
     * @return 影响行数
     */
    public int consumeComputingPowerByPhone(@Param("phone") String phone, @Param("value") java.math.BigDecimal value);
}
