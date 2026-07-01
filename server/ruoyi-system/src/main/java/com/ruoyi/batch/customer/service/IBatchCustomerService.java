package com.ruoyi.batch.customer.service;

import java.util.List;
import com.ruoyi.batch.customer.domain.BatchCustomer;

/**
 * 客户/APP账号Service接口
 *
 * @author ruoyi
 */
public interface IBatchCustomerService
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
     * 校验手机号唯一
     *
     * @param phone 手机号
     * @param excludeId 排除的客户ID
     * @return 是否唯一
     */
    public boolean checkPhoneUnique(String phone, Long excludeId);

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
     * 按上级手机号查询下级列表
     *
     * @param parentPhone 上级手机号
     * @return 下级列表
     */
    public List<BatchCustomer> selectByParentPhone(String parentPhone);

    /**
     * 生成/重置客户注册二维码
     *
     * @param customerId 客户ID
     * @return 二维码URL
     */
    public String generateQrCode(Long customerId);

    /**
     * 账号升级
     *
     * @param customerId 客户ID
     * @param newParentPhone 新上级手机号
     * @param maxServiceProvider 分公司最大可创建服务商数量
     * @param totalIndividualCapacity 分公司旗下服务商总可分配个人账号容量
     * @param maxIndividual 服务商可拆分创建个人账号上限
     * @return 影响行数
     */
    public int upgradeCustomer(Long customerId, String newParentPhone, Integer maxServiceProvider,
                               Integer totalIndividualCapacity, Integer maxIndividual);

    /**
     * 账号迁移（修改上级）
     *
     * @param customerId 客户ID
     * @param newParentPhone 新上级手机号
     * @return 影响行数
     */
    public int migrateCustomer(Long customerId, String newParentPhone);

    /**
     * 修改客户状态
     *
     * @param batchCustomer 客户信息
     * @return 影响行数
     */
    public int updateStatus(BatchCustomer batchCustomer);
}
