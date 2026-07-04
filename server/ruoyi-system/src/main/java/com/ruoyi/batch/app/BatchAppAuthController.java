package com.ruoyi.batch.app;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.batch.customer.domain.BatchCustomer;
import com.ruoyi.batch.customer.service.IBatchCustomerService;

/**
 * APP 端认证与客户信息接口
 *
 * @author ruoyi
 */
@Validated
@RestController
@RequestMapping("/batch/app")
public class BatchAppAuthController extends BaseController
{
    /** 账号类型：个人 */
    private static final int TYPE_INDIVIDUAL = 3;

    /** 状态：启用 */
    private static final int STATUS_ENABLE = 0;

    @Autowired
    private IBatchCustomerService customerService;

    @Autowired
    private BatchAppTokenService tokenService;

    /**
     * APP 手机号+密码登录
     */
    @PostMapping("/login")
    public AjaxResult login(@Valid @RequestBody AppLoginBody loginBody)
    {
        BatchCustomer customer = customerService.selectBatchCustomerByPhone(loginBody.getPhone());
        if (customer == null)
        {
            return AjaxResult.error("手机号或密码错误");
        }
        if (STATUS_ENABLE != customer.getStatus())
        {
            return AjaxResult.error("账号已被禁用，请联系管理员");
        }
        if (!SecurityUtils.matchesPassword(loginBody.getPassword(), customer.getPassword()))
        {
            return AjaxResult.error("手机号或密码错误");
        }

        String token = createToken(customer);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("token", token);
        ajax.put(AjaxResult.DATA_TAG, customer);
        return ajax;
    }

    /**
     * APP 注册
     */
    @PostMapping("/register")
    public AjaxResult register(@Valid @RequestBody AppRegisterBody registerBody)
    {
        if (!customerService.checkPhoneUnique(registerBody.getPhone(), null))
        {
            return AjaxResult.error("该手机号已被注册");
        }

        BatchCustomer customer = new BatchCustomer();
        customer.setPhone(registerBody.getPhone());
        customer.setPassword(SecurityUtils.encryptPassword(registerBody.getPassword()));
        customer.setCustomerType(TYPE_INDIVIDUAL);
        customer.setCustomerName(registerBody.getPhone());
        customer.setContactName(registerBody.getPhone());
        customer.setParentPhone(StringUtils.isEmpty(registerBody.getParentPhone()) ? "" : registerBody.getParentPhone());
        customer.setComputingPowerTotal(BigDecimal.ZERO);
        customer.setVipExpireDate(new Date());

        customerService.registerAppCustomer(customer);

        String token = createToken(customer);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("token", token);
        ajax.put(AjaxResult.DATA_TAG, customer);
        return ajax;
    }

    /**
     * 根据手机号查询当前客户信息（需要 JWT）
     */
    @GetMapping("/customer/phone/{phone}")
    public AjaxResult getCustomerByPhone(@PathVariable("phone") String phone)
    {
        LoginUser loginUser = getLoginUser();
        if (!phone.equals(loginUser.getUsername()))
        {
            return AjaxResult.error("无权查看其他账号信息");
        }
        BatchCustomer customer = customerService.selectBatchCustomerByPhone(phone);
        return AjaxResult.success(customer);
    }

    /**
     * 更新客户信息（需要 JWT）
     */
    @PutMapping("/customer")
    public AjaxResult updateCustomer(@RequestBody BatchCustomer customer)
    {
        LoginUser loginUser = getLoginUser();
        Long customerId = loginUser.getUserId();
        if (customerId == null)
        {
            return AjaxResult.error("登录信息异常");
        }

        BatchCustomer update = new BatchCustomer();
        update.setCustomerId(customerId);
        update.setCustomerName(customer.getCustomerName());
        update.setContactName(customer.getContactName());
        // 禁止修改敏感字段
        update.setPhone(null);
        update.setPassword(null);
        update.setCustomerType(null);
        update.setParentPhone(null);
        update.setBranchPhone(null);
        update.setComputingPowerTotal(null);
        update.setComputingPowerUsed(null);
        update.setComputingPowerRemain(null);

        return toAjax(customerService.updateBatchCustomer(update));
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public AjaxResult logout(@RequestHeader(value = "Authorization", required = false) String authorization)
    {
        if (StringUtils.isNotEmpty(authorization) && authorization.startsWith(Constants.TOKEN_PREFIX))
        {
            String token = authorization.replace(Constants.TOKEN_PREFIX, "");
            tokenService.delLoginUser(token);
        }
        return AjaxResult.success();
    }

    /**
     * APP 文件上传（头像等）
     */
    @PostMapping("/upload")
    public AjaxResult uploadFile(MultipartFile file) throws Exception
    {
        if (file == null || file.isEmpty())
        {
            return AjaxResult.error("上传文件不能为空");
        }
        String filePath = RuoYiConfig.getUploadPath();
        String fileName = FileUploadUtils.upload(filePath, file);
        String url = buildFileUrl(fileName);
        return AjaxResult.success(url);
    }

    /**
     * APP 自助注销当前账号
     */
    @DeleteMapping("/customer")
    public AjaxResult deleteCurrentCustomer(@RequestHeader(value = "Authorization", required = false) String authorization)
    {
        LoginUser loginUser = getLoginUser();
        Long customerId = loginUser.getUserId();
        if (customerId == null)
        {
            return AjaxResult.error("登录信息异常");
        }

        BatchCustomer customer = customerService.selectBatchCustomerById(customerId);
        if (customer == null)
        {
            return AjaxResult.error("账号不存在");
        }

        int subordinateCount = customerService.countByParentPhone(customer.getPhone());
        if (subordinateCount > 0)
        {
            return AjaxResult.error("当前账号存在下级账号，无法注销");
        }

        int rows = customerService.deleteBatchCustomerByIds(new Long[] { customerId });

        // 注销后清理登录缓存
        if (StringUtils.isNotEmpty(authorization) && authorization.startsWith(Constants.TOKEN_PREFIX))
        {
            String token = authorization.replace(Constants.TOKEN_PREFIX, "");
            tokenService.delLoginUser(token);
        }
        return toAjax(rows);
    }

    /**
     * 根据文件名构造完整访问 URL
     */
    private String buildFileUrl(String fileName)
    {
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null)
        {
            return fileName;
        }
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getServletContext().getContextPath();
        return url.delete(url.length() - request.getRequestURI().length(), url.length())
                .append(contextPath).append(fileName).toString();
    }

    /**
     * 构造 LoginUser 并生成 JWT
     */
    private String createToken(BatchCustomer customer)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(customer.getCustomerId());
        sysUser.setUserName(customer.getPhone());
        sysUser.setNickName(customer.getCustomerName());
        sysUser.setPhonenumber(customer.getPhone());
        sysUser.setPassword(customer.getPassword());
        sysUser.setStatus(String.valueOf(customer.getStatus()));
        sysUser.setAvatar("");
        sysUser.setEmail("");
        sysUser.setSex("2");

        Set<String> permissions = new HashSet<>();
        permissions.add("app:user");

        LoginUser loginUser = new LoginUser(customer.getCustomerId(), 0L, sysUser, permissions);
        return tokenService.createToken(loginUser);
    }

    /**
     * APP 登录请求体
     */
    public static class AppLoginBody
    {
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        @NotBlank(message = "密码不能为空")
        private String password;

        public String getPhone()
        {
            return phone;
        }

        public void setPhone(String phone)
        {
            this.phone = phone;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }
    }

    /**
     * APP 注册请求体
     */
    public static class AppRegisterBody
    {
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        @NotBlank(message = "密码不能为空")
        private String password;

        private String parentPhone;

        public String getPhone()
        {
            return phone;
        }

        public void setPhone(String phone)
        {
            this.phone = phone;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }

        public String getParentPhone()
        {
            return parentPhone;
        }

        public void setParentPhone(String parentPhone)
        {
            this.parentPhone = parentPhone;
        }
    }
}
