package com.ruoyi.batch.customer.service.impl;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.batch.customer.domain.BatchCustomer;
import com.ruoyi.batch.customer.domain.BatchCustomerImportFail;
import com.ruoyi.batch.customer.domain.BatchCustomerImportResult;
import com.ruoyi.batch.customer.mapper.BatchCustomerMapper;
import com.ruoyi.batch.customer.service.IBatchCustomerService;
import com.ruoyi.batch.customer.service.IBatchQrCodeStatService;
import com.ruoyi.batch.customer.utils.QrCodeUtil;

/**
 * 客户/APP账号Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class BatchCustomerServiceImpl implements IBatchCustomerService
{
    /** 账号类型：分公司 */
    private static final int TYPE_BRANCH = 1;

    /** 账号类型：服务商 */
    private static final int TYPE_SERVICE_PROVIDER = 2;

    /** 账号类型：个人 */
    private static final int TYPE_INDIVIDUAL = 3;

    /** 状态：启用 */
    private static final int STATUS_ENABLE = 0;

    /** 删除标志：存在 */
    private static final int DEL_FLAG_EXIST = 0;

    @Autowired
    private BatchCustomerMapper batchCustomerMapper;

    @Autowired
    private QrCodeUtil qrCodeUtil;

    @Autowired
    private IBatchQrCodeStatService qrCodeStatService;

    /** 后台新增客户时的初始密码（配置项 batch.app.default-password） */
    @Value("${batch.app.default-password:123456}")
    private String defaultPassword;

    @Override
    public List<BatchCustomer> selectBatchCustomerList(BatchCustomer batchCustomer)
    {
        return batchCustomerMapper.selectBatchCustomerList(batchCustomer);
    }

    @Override
    public BatchCustomer selectBatchCustomerById(Long customerId)
    {
        BatchCustomer customer = batchCustomerMapper.selectBatchCustomerById(customerId);
        if (customer != null)
        {
            fillSubordinateCount(customer);
        }
        return customer;
    }

    @Override
    public BatchCustomer selectBatchCustomerByPhone(String phone)
    {
        return batchCustomerMapper.selectBatchCustomerByPhone(phone);
    }

    @Override
    public boolean checkPhoneUnique(String phone, Long excludeId)
    {
        return batchCustomerMapper.checkPhoneUnique(phone, excludeId) == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBatchCustomer(BatchCustomer batchCustomer)
    {
        validateInsert(batchCustomer);

        // 未指定密码时使用初始密码，统一 BCrypt 加密入库，保证账号可直接登录
        String rawPassword = StringUtils.isEmpty(batchCustomer.getPassword()) ? defaultPassword : batchCustomer.getPassword();
        batchCustomer.setPassword(SecurityUtils.encryptPassword(rawPassword));

        batchCustomer.setDelFlag(DEL_FLAG_EXIST);
        batchCustomer.setStatus(STATUS_ENABLE);
        batchCustomer.setComputingPowerUsed(BigDecimal.ZERO);
        batchCustomer.setComputingPowerRemain(batchCustomer.getComputingPowerTotal());

        // 分公司无上级、无分公司手机号
        if (TYPE_BRANCH == batchCustomer.getCustomerType())
        {
            batchCustomer.setParentPhone("");
            batchCustomer.setBranchPhone("");
        }
        else
        {
            BatchCustomer parent = selectBatchCustomerByPhone(batchCustomer.getParentPhone());
            batchCustomer.setBranchPhone(buildBranchPhone(parent, batchCustomer));
        }

        batchCustomer.setCreateTime(DateUtils.getNowDate());
        int rows = batchCustomerMapper.insertBatchCustomer(batchCustomer);

        // 生成二维码
        String qrCodeUrl = generateQrCode(batchCustomer.getCustomerId());
        batchCustomer.setQrCodeUrl(qrCodeUrl);

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int registerAppCustomer(BatchCustomer batchCustomer)
    {
        if (!checkPhoneUnique(batchCustomer.getPhone(), null))
        {
            throw new ServiceException("该手机号已被注册");
        }

        batchCustomer.setCustomerType(TYPE_INDIVIDUAL);
        batchCustomer.setDelFlag(DEL_FLAG_EXIST);
        batchCustomer.setStatus(STATUS_ENABLE);
        batchCustomer.setComputingPowerUsed(BigDecimal.ZERO);
        batchCustomer.setComputingPowerRemain(batchCustomer.getComputingPowerTotal());

        if (StringUtils.isNotEmpty(batchCustomer.getParentPhone()))
        {
            BatchCustomer parent = selectBatchCustomerByPhone(batchCustomer.getParentPhone());
            validateParent(parent, TYPE_INDIVIDUAL);
            checkParentQuota(parent, TYPE_INDIVIDUAL);
            batchCustomer.setBranchPhone(buildBranchPhone(parent, batchCustomer));
        }
        else
        {
            batchCustomer.setBranchPhone("");
        }

        batchCustomer.setCreateTime(DateUtils.getNowDate());
        int rows = batchCustomerMapper.insertBatchCustomer(batchCustomer);

        // 生成二维码
        String qrCodeUrl = generateQrCode(batchCustomer.getCustomerId());
        batchCustomer.setQrCodeUrl(qrCodeUrl);

        // 有上级手机号时，上级二维码的注册次数+1
        if (StringUtils.isNotEmpty(batchCustomer.getParentPhone()))
        {
            qrCodeStatService.incrementRegisterCount(batchCustomer.getParentPhone());
        }

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBatchCustomer(BatchCustomer batchCustomer)
    {
        BatchCustomer original = selectBatchCustomerById(batchCustomer.getCustomerId());
        if (original == null)
        {
            throw new ServiceException("客户不存在");
        }

        // 编辑不允许修改 customerType 和 phone
        batchCustomer.setCustomerType(null);
        batchCustomer.setPhone(null);
        batchCustomer.setUpdateTime(DateUtils.getNowDate());

        // 配额不能小于已创建数量
        if (TYPE_BRANCH == original.getCustomerType())
        {
            int usedServiceProvider = countByParentPhone(original.getPhone());
            if (batchCustomer.getMaxServiceProvider() != null
                    && batchCustomer.getMaxServiceProvider() < usedServiceProvider)
            {
                throw new ServiceException("最大可创建服务商数量不能小于已创建数量");
            }
            int usedIndividual = countIndividualByBranchPhone(original.getPhone());
            if (batchCustomer.getTotalIndividualCapacity() != null
                    && batchCustomer.getTotalIndividualCapacity() < usedIndividual)
            {
                throw new ServiceException("旗下服务商总可分配个人账号容量不能小于已分配数量");
            }
        }
        else if (TYPE_SERVICE_PROVIDER == original.getCustomerType())
        {
            int usedIndividual = countByParentPhone(original.getPhone());
            if (batchCustomer.getMaxIndividual() != null
                    && batchCustomer.getMaxIndividual() < usedIndividual)
            {
                throw new ServiceException("可拆分创建个人账号上限不能小于已创建数量");
            }
        }

        // 算力总配额变更时同步剩余算力
        if (batchCustomer.getComputingPowerTotal() != null)
        {
            BigDecimal used = original.getComputingPowerUsed() != null ? original.getComputingPowerUsed() : BigDecimal.ZERO;
            batchCustomer.setComputingPowerRemain(batchCustomer.getComputingPowerTotal().subtract(used));
        }

        return batchCustomerMapper.updateBatchCustomer(batchCustomer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatchCustomerByIds(Long[] customerIds)
    {
        for (Long customerId : customerIds)
        {
            BatchCustomer customer = selectBatchCustomerById(customerId);
            if (customer == null)
            {
                continue;
            }
            int subordinateCount = countByParentPhone(customer.getPhone());
            if (subordinateCount > 0)
            {
                throw new ServiceException("账号【" + customer.getCustomerName() + "】存在下级账号，请先迁移或清空下级");
            }
        }
        return batchCustomerMapper.deleteBatchCustomerByIds(customerIds);
    }

    @Override
    public int countByParentPhone(String parentPhone)
    {
        return batchCustomerMapper.countByParentPhone(parentPhone);
    }

    @Override
    public List<BatchCustomer> selectByParentPhone(String parentPhone)
    {
        return batchCustomerMapper.selectByParentPhone(parentPhone);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateQrCode(Long customerId)
    {
        BatchCustomer customer = selectBatchCustomerById(customerId);
        if (customer == null)
        {
            throw new ServiceException("客户不存在");
        }
        String url = qrCodeUtil.generateQrCode(customer.getPhone());

        BatchCustomer update = new BatchCustomer();
        update.setCustomerId(customerId);
        update.setQrCodeUrl(url);
        update.setQrCodeKey(IdUtils.fastUUID());
        batchCustomerMapper.updateQrCode(update);
        return url;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int upgradeCustomer(Long customerId, String newParentPhone, Integer maxServiceProvider,
                               Integer totalIndividualCapacity, Integer maxIndividual)
    {
        BatchCustomer customer = selectBatchCustomerById(customerId);
        if (customer == null)
        {
            throw new ServiceException("客户不存在");
        }

        int currentType = customer.getCustomerType();
        int newType;
        if (TYPE_INDIVIDUAL == currentType)
        {
            newType = TYPE_SERVICE_PROVIDER;
            // 新上级必须是分公司
            BatchCustomer parent = selectBatchCustomerByPhone(newParentPhone);
            if (parent == null || TYPE_BRANCH != parent.getCustomerType())
            {
                throw new ServiceException("新上级手机号不存在或类型不匹配");
            }
            checkParentQuota(parent, newType);
            if (maxIndividual == null || maxIndividual < 0)
            {
                throw new ServiceException("请配置服务商可拆分创建个人账号上限");
            }
        }
        else if (TYPE_SERVICE_PROVIDER == currentType)
        {
            newType = TYPE_BRANCH;
            // 升级为分公司后无上级
            newParentPhone = "";
            if (maxServiceProvider == null || maxServiceProvider < 0 || totalIndividualCapacity == null || totalIndividualCapacity < 0)
            {
                throw new ServiceException("请配置分公司配额");
            }
        }
        else
        {
            throw new ServiceException("分公司账号不可升级");
        }

        BatchCustomer update = new BatchCustomer();
        update.setCustomerId(customerId);
        update.setCustomerType(newType);
        update.setParentPhone(newParentPhone);
        update.setBranchPhone(buildBranchPhone(selectBatchCustomerByPhone(newParentPhone), customer));
        update.setMaxServiceProvider(maxServiceProvider);
        update.setTotalIndividualCapacity(totalIndividualCapacity);
        update.setMaxIndividual(maxIndividual);
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = batchCustomerMapper.updateBatchCustomer(update);

        // 同步更新所有下级的 branch_phone
        syncSubordinateBranchPhone(customer.getPhone(), update.getBranchPhone());

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int migrateCustomer(Long customerId, String newParentPhone)
    {
        BatchCustomer customer = selectBatchCustomerById(customerId);
        if (customer == null)
        {
            throw new ServiceException("客户不存在");
        }

        if (TYPE_BRANCH == customer.getCustomerType())
        {
            throw new ServiceException("分公司账号不可迁移");
        }

        // 新上级不能是自己
        if (newParentPhone.equals(customer.getPhone()))
        {
            throw new ServiceException("新上级手机号不能是当前账号本身");
        }

        BatchCustomer parent = selectBatchCustomerByPhone(newParentPhone);
        validateParent(parent, customer.getCustomerType());

        // 迁移时排除当前账号自身
        int currentCount = countByParentPhone(newParentPhone);
        if (parent.getPhone().equals(customer.getParentPhone()))
        {
            currentCount--;
        }
        checkParentQuotaInternal(parent, customer.getCustomerType(), currentCount);

        BatchCustomer update = new BatchCustomer();
        update.setCustomerId(customerId);
        update.setParentPhone(newParentPhone);
        update.setBranchPhone(buildBranchPhone(parent, customer));
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = batchCustomerMapper.updateBatchCustomer(update);

        // 同步更新所有下级的 branch_phone
        syncSubordinateBranchPhone(customer.getPhone(), update.getBranchPhone());

        return rows;
    }

    @Override
    public int updateStatus(BatchCustomer batchCustomer)
    {
        batchCustomer.setUpdateTime(DateUtils.getNowDate());
        return batchCustomerMapper.updateBatchCustomer(batchCustomer);
    }

    /**
     * 新增时校验
     */
    private void validateInsert(BatchCustomer batchCustomer)
    {
        if (batchCustomer.getCustomerType() == null)
        {
            throw new ServiceException("账号类型不能为空");
        }
        if (!checkPhoneUnique(batchCustomer.getPhone(), null))
        {
            throw new ServiceException("该手机号已被注册");
        }

        int type = batchCustomer.getCustomerType();
        if (TYPE_BRANCH == type)
        {
            if (batchCustomer.getMaxServiceProvider() == null || batchCustomer.getTotalIndividualCapacity() == null)
            {
                throw new ServiceException("分公司配额不能为空");
            }
        }
        else
        {
            if (StringUtils.isEmpty(batchCustomer.getParentPhone()))
            {
                throw new ServiceException("上级手机号不能为空");
            }
            BatchCustomer parent = selectBatchCustomerByPhone(batchCustomer.getParentPhone());
            validateParent(parent, type);
            checkParentQuota(parent, type);
        }
    }

    /**
     * 校验上级合法性
     */
    private void validateParent(BatchCustomer parent, int childType)
    {
        if (parent == null)
        {
            throw new ServiceException("上级手机号不存在");
        }
        if (TYPE_SERVICE_PROVIDER == childType && TYPE_BRANCH != parent.getCustomerType())
        {
            throw new ServiceException("上级手机号类型不匹配，服务商的上级必须是分公司");
        }
        if (TYPE_INDIVIDUAL == childType && TYPE_SERVICE_PROVIDER != parent.getCustomerType())
        {
            throw new ServiceException("上级手机号类型不匹配，个人的上级必须是服务商");
        }
    }

    /**
     * 校验上级名额是否充足
     */
    private void checkParentQuota(BatchCustomer parent, int childType)
    {
        int currentCount = countByParentPhone(parent.getPhone());
        checkParentQuotaInternal(parent, childType, currentCount);
    }

    private void checkParentQuotaInternal(BatchCustomer parent, int childType, int currentCount)
    {
        if (TYPE_BRANCH == parent.getCustomerType() && TYPE_SERVICE_PROVIDER == childType)
        {
            int max = parent.getMaxServiceProvider() != null ? parent.getMaxServiceProvider() : 0;
            if (currentCount >= max)
            {
                throw new ServiceException("该上级可创建下级名额已满");
            }
        }
        else if (TYPE_SERVICE_PROVIDER == parent.getCustomerType() && TYPE_INDIVIDUAL == childType)
        {
            int max = parent.getMaxIndividual() != null ? parent.getMaxIndividual() : 0;
            if (currentCount >= max)
            {
                throw new ServiceException("该上级可创建下级名额已满");
            }
        }
    }

    /**
     * 构建所属分公司手机号
     */
    private String buildBranchPhone(BatchCustomer parent, BatchCustomer child)
    {
        if (TYPE_BRANCH == child.getCustomerType())
        {
            return "";
        }
        if (parent == null)
        {
            return "";
        }
        if (TYPE_BRANCH == parent.getCustomerType())
        {
            return parent.getPhone();
        }
        return parent.getBranchPhone();
    }

    /**
     * 同步更新所有下级的所属分公司手机号
     */
    private void syncSubordinateBranchPhone(String parentPhone, String branchPhone)
    {
        if (StringUtils.isEmpty(parentPhone))
        {
            return;
        }
        List<BatchCustomer> subordinates = selectByParentPhone(parentPhone);
        for (BatchCustomer subordinate : subordinates)
        {
            BatchCustomer update = new BatchCustomer();
            update.setCustomerId(subordinate.getCustomerId());
            update.setBranchPhone(branchPhone);
            update.setUpdateTime(DateUtils.getNowDate());
            batchCustomerMapper.updateBatchCustomer(update);
            syncSubordinateBranchPhone(subordinate.getPhone(), branchPhone);
        }
    }

    /**
     * 填充下级数量
     */
    private void fillSubordinateCount(BatchCustomer customer)
    {
        int count = countByParentPhone(customer.getPhone());
        customer.setSubordinateCount(count);
    }

    /**
     * 按分公司手机号统计旗下个人账号数量
     */
    private int countIndividualByBranchPhone(String branchPhone)
    {
        BatchCustomer query = new BatchCustomer();
        query.setBranchPhone(branchPhone);
        query.setCustomerType(TYPE_INDIVIDUAL);
        List<BatchCustomer> list = selectBatchCustomerList(query);
        return list.size();
    }

    @Override
    public BatchCustomerImportResult importCustomer(InputStream inputStream)
    {
        ExcelUtil<BatchCustomer> util = new ExcelUtil<BatchCustomer>(BatchCustomer.class);
        List<BatchCustomer> list = util.importExcel(inputStream);
        BatchCustomerImportResult result = new BatchCustomerImportResult();

        for (int i = 0; i < list.size(); i++)
        {
            BatchCustomer customer = list.get(i);
            int rowNum = i + 2; // 第1行为表头

            try
            {
                // 前置基础校验：必填项缺失直接进失败列表，不入库
                validateImportCustomer(customer, rowNum);

                // 默认值
                if (customer.getStatus() == null)
                {
                    customer.setStatus(STATUS_ENABLE);
                }

                insertBatchCustomer(customer);
                result.addSuccess();
            }
            catch (Exception e)
            {
                BatchCustomerImportFail fail = new BatchCustomerImportFail();
                fail.setRowNum(rowNum);
                fail.setCustomerName(customer.getCustomerName());
                fail.setPhone(customer.getPhone());
                fail.setReason(e.getMessage());
                result.addFail(fail);
            }
        }

        return result;
    }

    /**
     * 导入前置校验
     */
    private void validateImportCustomer(BatchCustomer customer, int rowNum)
    {
        if (customer.getCustomerType() == null)
        {
            throw new ServiceException("第" + rowNum + "行：账号类型不能为空");
        }
        if (StringUtils.isEmpty(customer.getCustomerName()))
        {
            throw new ServiceException("第" + rowNum + "行：账号名称不能为空");
        }
        if (StringUtils.isEmpty(customer.getContactName()))
        {
            throw new ServiceException("第" + rowNum + "行：联系人不能为空");
        }
        if (StringUtils.isEmpty(customer.getPhone()))
        {
            throw new ServiceException("第" + rowNum + "行：手机号不能为空");
        }
        if (customer.getComputingPowerTotal() == null)
        {
            throw new ServiceException("第" + rowNum + "行：算力总配额不能为空");
        }
        if (customer.getVipExpireDate() == null)
        {
            throw new ServiceException("第" + rowNum + "行：VIP有效期不能为空");
        }

        int type = customer.getCustomerType();
        if (TYPE_BRANCH == type)
        {
            if (customer.getMaxServiceProvider() == null)
            {
                throw new ServiceException("第" + rowNum + "行：最大服务商数量不能为空");
            }
            if (customer.getTotalIndividualCapacity() == null)
            {
                throw new ServiceException("第" + rowNum + "行：个人账号总容量不能为空");
            }
        }
        else
        {
            if (StringUtils.isEmpty(customer.getParentPhone()))
            {
                throw new ServiceException("第" + rowNum + "行：上级手机号不能为空");
            }
            if (TYPE_SERVICE_PROVIDER == type && customer.getMaxIndividual() == null)
            {
                throw new ServiceException("第" + rowNum + "行：个人账号上限不能为空");
            }
        }
    }
}
