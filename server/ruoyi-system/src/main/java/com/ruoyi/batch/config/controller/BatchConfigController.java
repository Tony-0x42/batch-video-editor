package com.ruoyi.batch.config.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.batch.config.domain.BatchAppVersion;
import com.ruoyi.batch.config.domain.BatchSystemConfig;
import com.ruoyi.batch.config.service.IBatchAppVersionService;
import com.ruoyi.batch.config.service.IBatchSystemConfigService;

/**
 * 系统配置 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/batch/config")
public class BatchConfigController extends BaseController
{
    @Autowired
    private IBatchSystemConfigService configService;

    @Autowired
    private IBatchAppVersionService versionService;

    /** 品牌配置相关参数键 */
    private static final String[] BRAND_CONFIG_KEYS = {
        "batch.brand.appLogo",
        "batch.brand.adminLogo",
        "batch.brand.productName",
        "batch.brand.slogan",
        "batch.brand.primaryColor",
        "batch.brand.loginBg"
    };

    /** 全局参数相关参数键 */
    private static final String[] GLOBAL_CONFIG_KEYS = {
        "batch.ai.maxVideos",
        "batch.ai.sliceMin",
        "batch.ai.sliceMax",
        "batch.ai.sliceStep",
        "batch.computing.emptyTip",
        "batch.link.parseFailTip",
        "batch.global.emptyPlaceholder",
        "batch.global.customerServiceHours"
    };

    /** 全局参数默认值：单次生成视频数量上限 */
    private static final int DEFAULT_MAX_VIDEOS = 10;

    /** 全局参数默认值：切片时长下限（秒） */
    private static final double DEFAULT_SLICE_MIN = 0.5;

    /** 全局参数默认值：切片时长上限（秒） */
    private static final double DEFAULT_SLICE_MAX = 10.0;

    /** 全局参数默认值：切片时长步长（秒） */
    private static final double DEFAULT_SLICE_STEP = 0.1;

    /** 全局参数默认值：算力耗尽提示文案 */
    private static final String DEFAULT_COMPUTING_EMPTY_TIP = "当前算力已耗尽，请联系管理员增加算力额度";

    /** 全局参数默认值：链接解析失败提示文案 */
    private static final String DEFAULT_PARSE_FAIL_TIP = "链接解析失败，请检查链接是否有效";

    /**
     * 查询品牌配置
     */
    @PreAuthorize("isAnonymous() or @ss.hasPermi('batch:config:list') or @ss.hasPermi('app:user')")
    @GetMapping("/brand")
    public AjaxResult getBrandConfig()
    {
        Map<String, String> configMap = configService.selectBatchSystemConfigMapByGroup("brand");
        Map<String, Object> result = new HashMap<>();
        for (String key : BRAND_CONFIG_KEYS)
        {
            String shortKey = key.substring(key.lastIndexOf(".") + 1);
            result.put(shortKey, configMap.getOrDefault(key, ""));
        }
        return success(result);
    }

    /**
     * 保存品牌配置
     */
    @PreAuthorize("@ss.hasPermi('batch:config:edit')")
    @Log(title = "系统配置-品牌配置", businessType = BusinessType.UPDATE)
    @PostMapping("/brand")
    public AjaxResult saveBrandConfig(@RequestBody Map<String, String> form)
    {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("batch.brand.appLogo", form.getOrDefault("appLogo", ""));
        configMap.put("batch.brand.adminLogo", form.getOrDefault("adminLogo", ""));
        configMap.put("batch.brand.productName", form.getOrDefault("productName", ""));
        configMap.put("batch.brand.slogan", form.getOrDefault("slogan", ""));
        configMap.put("batch.brand.primaryColor", form.getOrDefault("primaryColor", ""));
        configMap.put("batch.brand.loginBg", form.getOrDefault("loginBg", ""));
        return toAjax(configService.saveConfigGroup("brand", configMap, getUsername()));
    }

    /**
     * 查询全局参数
     */
    @PreAuthorize("isAnonymous() or @ss.hasPermi('batch:config:list') or @ss.hasPermi('app:user')")
    @GetMapping("/global")
    public AjaxResult getGlobalConfig()
    {
        Map<String, String> configMap = configService.selectBatchSystemConfigMapByGroup("global");
        Map<String, Object> result = new HashMap<>();
        for (String key : GLOBAL_CONFIG_KEYS)
        {
            String shortKey = key.substring(key.lastIndexOf(".") + 1);
            String value = configMap.getOrDefault(key, "");
            if ("maxVideos".equals(shortKey) || "sliceMin".equals(shortKey) || "sliceMax".equals(shortKey) || "sliceStep".equals(shortKey))
            {
                try
                {
                    result.put(shortKey, "maxVideos".equals(shortKey) ? Integer.parseInt(value) : Double.parseDouble(value));
                }
                catch (NumberFormatException e)
                {
                    result.put(shortKey, defaultNumberValue(shortKey));
                }
            }
            else
            {
                result.put(shortKey, value);
            }
        }
        return success(result);
    }

    /**
     * 数值型全局参数的默认值
     */
    private Object defaultNumberValue(String shortKey)
    {
        switch (shortKey)
        {
            case "maxVideos":
                return DEFAULT_MAX_VIDEOS;
            case "sliceMin":
                return DEFAULT_SLICE_MIN;
            case "sliceMax":
                return DEFAULT_SLICE_MAX;
            default:
                return DEFAULT_SLICE_STEP;
        }
    }

    /**
     * 保存全局参数
     */
    @PreAuthorize("@ss.hasPermi('batch:config:edit')")
    @Log(title = "系统配置-全局参数", businessType = BusinessType.UPDATE)
    @PostMapping("/global")
    public AjaxResult saveGlobalConfig(@RequestBody Map<String, Object> form)
    {
        Map<String, String> defaults = defaultGlobalConfigMap();
        Map<String, String> configMap = new HashMap<>();
        for (String key : GLOBAL_CONFIG_KEYS)
        {
            String shortKey = key.substring(key.lastIndexOf(".") + 1);
            configMap.put(key, String.valueOf(form.getOrDefault(shortKey, defaults.get(key))));
        }
        return toAjax(configService.saveConfigGroup("global", configMap, getUsername()));
    }

    /**
     * 初始化全局参数默认值
     */
    @PreAuthorize("@ss.hasPermi('batch:config:add')")
    @Log(title = "系统配置-初始化全局参数", businessType = BusinessType.INSERT)
    @PostMapping("/initGlobal")
    public AjaxResult initGlobalConfig()
    {
        return toAjax(configService.saveConfigGroup("global", defaultGlobalConfigMap(), getUsername()));
    }

    /**
     * 全局参数默认值映射
     */
    private Map<String, String> defaultGlobalConfigMap()
    {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("batch.ai.maxVideos", String.valueOf(DEFAULT_MAX_VIDEOS));
        configMap.put("batch.ai.sliceMin", String.valueOf(DEFAULT_SLICE_MIN));
        configMap.put("batch.ai.sliceMax", String.valueOf(DEFAULT_SLICE_MAX));
        configMap.put("batch.ai.sliceStep", String.valueOf(DEFAULT_SLICE_STEP));
        configMap.put("batch.computing.emptyTip", DEFAULT_COMPUTING_EMPTY_TIP);
        configMap.put("batch.link.parseFailTip", DEFAULT_PARSE_FAIL_TIP);
        configMap.put("batch.global.emptyPlaceholder", "");
        configMap.put("batch.global.customerServiceHours", "");
        return configMap;
    }

    /**
     * 查询APP版本列表
     */
    @PreAuthorize("@ss.hasPermi('batch:config:list')")
    @GetMapping("/version/list")
    public TableDataInfo versionList(BatchAppVersion batchAppVersion)
    {
        startPage();
        List<BatchAppVersion> list = versionService.selectBatchAppVersionList(batchAppVersion);
        return getDataTable(list);
    }

    /**
     * 导出APP版本列表
     */
    @PreAuthorize("@ss.hasPermi('batch:config:export')")
    @Log(title = "系统配置-版本管理", businessType = BusinessType.EXPORT)
    @GetMapping("/version/export")
    public void exportVersion(HttpServletResponse response, BatchAppVersion batchAppVersion)
    {
        List<BatchAppVersion> list = versionService.selectBatchAppVersionList(batchAppVersion);
        ExcelUtil<BatchAppVersion> util = new ExcelUtil<BatchAppVersion>(BatchAppVersion.class);
        util.exportExcel(response, list, "版本数据");
    }

    /**
     * 根据版本ID获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('batch:config:query')")
    @GetMapping("/version/{versionId}")
    public AjaxResult getVersionInfo(@PathVariable Long versionId)
    {
        return success(versionService.selectBatchAppVersionById(versionId));
    }

    /**
     * 新增APP版本
     */
    @PreAuthorize("@ss.hasPermi('batch:config:add')")
    @Log(title = "系统配置-版本管理", businessType = BusinessType.INSERT)
    @PostMapping("/version")
    public AjaxResult addVersion(@Validated @RequestBody BatchAppVersion batchAppVersion)
    {
        if (!versionService.checkVersionNoUnique(batchAppVersion))
        {
            return error("新增版本失败，该版本号已存在");
        }
        batchAppVersion.setCreateBy(getUsername());
        return toAjax(versionService.insertBatchAppVersion(batchAppVersion));
    }

    /**
     * 修改APP版本
     */
    @PreAuthorize("@ss.hasPermi('batch:config:edit')")
    @Log(title = "系统配置-版本管理", businessType = BusinessType.UPDATE)
    @PutMapping("/version")
    public AjaxResult editVersion(@Validated @RequestBody BatchAppVersion batchAppVersion)
    {
        if (!versionService.checkVersionNoUnique(batchAppVersion))
        {
            return error("修改版本失败，该版本号已存在");
        }
        batchAppVersion.setUpdateBy(getUsername());
        return toAjax(versionService.updateBatchAppVersion(batchAppVersion));
    }

    /**
     * 删除APP版本
     */
    @PreAuthorize("@ss.hasPermi('batch:config:remove')")
    @Log(title = "系统配置-版本管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/version/{versionIds}")
    public AjaxResult removeVersion(@PathVariable Long[] versionIds)
    {
        return toAjax(versionService.deleteBatchAppVersionByIds(versionIds));
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('batch:config:edit')")
    @Log(title = "系统配置-版本管理", businessType = BusinessType.UPDATE)
    @PutMapping("/version/changeStatus")
    public AjaxResult changeStatus(@RequestBody BatchAppVersion batchAppVersion)
    {
        batchAppVersion.setUpdateBy(getUsername());
        return toAjax(versionService.updateBatchAppVersion(batchAppVersion));
    }

    /**
     * 查询扩展全局参数列表
     */
    @PreAuthorize("@ss.hasPermi('batch:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(BatchSystemConfig batchSystemConfig)
    {
        startPage();
        List<BatchSystemConfig> list = configService.selectBatchSystemConfigList(batchSystemConfig);
        return getDataTable(list);
    }

    /**
     * 根据参数编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('batch:config:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable Long configId)
    {
        return success(configService.selectBatchSystemConfigById(configId));
    }

    /**
     * 新增扩展全局参数
     */
    @PreAuthorize("@ss.hasPermi('batch:config:add')")
    @Log(title = "系统配置-全局参数", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BatchSystemConfig batchSystemConfig)
    {
        batchSystemConfig.setCreateBy(getUsername());
        return toAjax(configService.insertBatchSystemConfig(batchSystemConfig));
    }

    /**
     * 修改扩展全局参数
     */
    @PreAuthorize("@ss.hasPermi('batch:config:edit')")
    @Log(title = "系统配置-全局参数", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BatchSystemConfig batchSystemConfig)
    {
        batchSystemConfig.setUpdateBy(getUsername());
        return toAjax(configService.updateBatchSystemConfig(batchSystemConfig));
    }

    /**
     * 删除扩展全局参数
     */
    @PreAuthorize("@ss.hasPermi('batch:config:remove')")
    @Log(title = "系统配置-全局参数", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        return toAjax(configService.deleteBatchSystemConfigByIds(configIds));
    }
}
