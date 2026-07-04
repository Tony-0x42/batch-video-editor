package com.example.cj.videoeditor.utils;

import android.content.Context;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.network.ApiCallback;
import com.example.cj.videoeditor.network.ApiService;

import java.util.HashMap;
import java.util.Map;

public class AppConfig {

    public static final String SP_LOGIN = "sp_login";
    public static final String SP_KEY_IS_LOGIN = "sp_key_is_login";
    public static final String SP_KEY_USER_PHONE = "sp_key_user_phone";
    public static final String SP_KEY_USER_NAME = "sp_key_user_name";
    public static final String SP_KEY_USER_AVATAR = "sp_key_user_avatar";
    public static final String SP_KEY_USER_VIP = "sp_key_user_vip";
    public static final String SP_KEY_USER_VIP_EXPIRE = "sp_key_user_vip_expire";
    public static final String SP_KEY_COMPUTE_TOTAL = "sp_key_compute_total";
    public static final String SP_KEY_COMPUTE_USED = "sp_key_compute_used";
    public static final String SP_KEY_COMPUTE_REMAIN = "sp_key_compute_remain";

    public static final String SP_KEY_CUSTOMER_ID = "sp_key_customer_id";
    public static final String SP_KEY_CUSTOMER_TYPE = "sp_key_customer_type";
    public static final String SP_KEY_PARENT_PHONE = "sp_key_parent_phone";

    // 后端 JWT Token
    public static final String SP_KEY_USER_ID = "sp_key_user_id";
    public static final String SP_KEY_TOKEN = "sp_key_token";

    // 品牌配置缓存
    public static final String SP_KEY_BRAND_APP_LOGO = "sp_key_brand_app_logo";
    public static final String SP_KEY_BRAND_PRODUCT_NAME = "sp_key_brand_product_name";
    public static final String SP_KEY_BRAND_SLOGAN = "sp_key_brand_slogan";
    public static final String SP_KEY_BRAND_LOGIN_BG = "sp_key_brand_login_bg";
    public static final String SP_KEY_BRAND_PRIMARY_COLOR = "sp_key_brand_primary_color";

    // 全局配置缓存
    public static final String SP_KEY_GLOBAL_MAX_VIDEOS = "sp_key_global_max_videos";
    public static final String SP_KEY_GLOBAL_SLICE_MIN = "sp_key_global_slice_min";
    public static final String SP_KEY_GLOBAL_SLICE_MAX = "sp_key_global_slice_max";
    public static final String SP_KEY_GLOBAL_SLICE_STEP = "sp_key_global_slice_step";
    public static final String SP_KEY_GLOBAL_POWER_EXHAUSTED_TIP = "sp_key_global_power_exhausted_tip";
    public static final String SP_KEY_GLOBAL_PARSE_FAIL_TIP = "sp_key_global_parse_fail_tip";
    public static final String SP_KEY_GLOBAL_EMPTY_PLACEHOLDER = "sp_key_global_empty_placeholder";
    public static final String SP_KEY_GLOBAL_CUSTOMER_SERVICE_HOURS = "sp_key_global_customer_service_hours";

    private AppConfig() {}

    /**
     * 缓存品牌配置到 SharedPreferences。
     */
    public static void saveBrandConfig(Context context, Map<String, Object> config) {
        if (context == null || config == null) return;
        SharedPrefUtil.putString(context, SP_KEY_BRAND_APP_LOGO, getValue(config, "appLogo"));
        SharedPrefUtil.putString(context, SP_KEY_BRAND_PRODUCT_NAME, getValue(config, "productName"));
        SharedPrefUtil.putString(context, SP_KEY_BRAND_SLOGAN, getValue(config, "slogan"));
        SharedPrefUtil.putString(context, SP_KEY_BRAND_LOGIN_BG, getValue(config, "loginBg"));
        SharedPrefUtil.putString(context, SP_KEY_BRAND_PRIMARY_COLOR, getValue(config, "primaryColor"));
    }

    /**
     * 从 SharedPreferences 读取品牌配置。
     */
    public static Map<String, Object> getBrandConfig(Context context) {
        Map<String, Object> config = new HashMap<>();
        if (context == null) return config;
        config.put("appLogo", SharedPrefUtil.getString(context, SP_KEY_BRAND_APP_LOGO, ""));
        config.put("productName", SharedPrefUtil.getString(context, SP_KEY_BRAND_PRODUCT_NAME, ""));
        config.put("slogan", SharedPrefUtil.getString(context, SP_KEY_BRAND_SLOGAN, ""));
        config.put("loginBg", SharedPrefUtil.getString(context, SP_KEY_BRAND_LOGIN_BG, ""));
        config.put("primaryColor", SharedPrefUtil.getString(context, SP_KEY_BRAND_PRIMARY_COLOR, ""));
        return config;
    }

    /**
     * 缓存全局配置到 SharedPreferences。
     */
    public static void saveGlobalConfig(Context context, Map<String, Object> config) {
        if (context == null || config == null) return;
        SharedPrefUtil.putInt(context, SP_KEY_GLOBAL_MAX_VIDEOS, parseInt(config.get("maxVideos"), 10));
        SharedPrefUtil.putString(context, SP_KEY_GLOBAL_SLICE_MIN, String.valueOf(parseDouble(config.get("sliceMin"), 0.5)));
        SharedPrefUtil.putString(context, SP_KEY_GLOBAL_SLICE_MAX, String.valueOf(parseDouble(config.get("sliceMax"), 10.0)));
        SharedPrefUtil.putString(context, SP_KEY_GLOBAL_SLICE_STEP, String.valueOf(parseDouble(config.get("sliceStep"), 0.1)));
        SharedPrefUtil.putString(context, SP_KEY_GLOBAL_POWER_EXHAUSTED_TIP, getValue(config, "emptyTip"));
        SharedPrefUtil.putString(context, SP_KEY_GLOBAL_PARSE_FAIL_TIP, getValue(config, "parseFailTip"));
        SharedPrefUtil.putString(context, SP_KEY_GLOBAL_EMPTY_PLACEHOLDER, getValue(config, "emptyPlaceholder"));
        SharedPrefUtil.putString(context, SP_KEY_GLOBAL_CUSTOMER_SERVICE_HOURS, getValue(config, "customerServiceHours"));
    }

    /**
     * 从后端加载全局配置并缓存，失败时保留本地已有配置/默认值。
     */
    public static void loadGlobalConfig(Context context, ApiService apiService) {
        if (context == null || apiService == null) return;
        apiService.getGlobalConfig().enqueue(new ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                saveGlobalConfig(context, data);
            }

            @Override
            public void onError(String msg) {
                // 静默失败，使用本地已缓存配置/默认值
            }
        });
    }

    /**
     * 算力耗尽提示文案（后端 key: emptyTip）。
     */
    public static String getPowerExhaustedMessage(Context context) {
        if (context == null) return "当前算力已耗尽，请联系管理员增加算力额度";
        return SharedPrefUtil.getString(context, SP_KEY_GLOBAL_POWER_EXHAUSTED_TIP,
                context.getString(R.string.power_exhausted));
    }

    /**
     * 链接解析失败提示文案（后端 key: parseFailTip）。
     */
    public static String getParseFailTip(Context context) {
        if (context == null) return "链接解析失败，请检查链接有效性或更换网络重试";
        return SharedPrefUtil.getString(context, SP_KEY_GLOBAL_PARSE_FAIL_TIP,
                context.getString(R.string.parse_failed));
    }

    /**
     * AI 云创单次选择视频上限 / 分镜头数量上限（后端 key: maxVideos）。
     */
    public static int getMaxVideos(Context context) {
        if (context == null) return 10;
        return SharedPrefUtil.getInt(context, SP_KEY_GLOBAL_MAX_VIDEOS, 10);
    }

    /**
     * 切片时长最小值（后端 key: sliceMin），单位秒。
     */
    public static double getSliceDurationMin(Context context) {
        if (context == null) return 0.5;
        return parseDoubleFromString(SharedPrefUtil.getString(context, SP_KEY_GLOBAL_SLICE_MIN, ""), 0.5);
    }

    /**
     * 切片时长最大值（后端 key: sliceMax），单位秒。
     */
    public static double getSliceDurationMax(Context context) {
        if (context == null) return 10.0;
        return parseDoubleFromString(SharedPrefUtil.getString(context, SP_KEY_GLOBAL_SLICE_MAX, ""), 10.0);
    }

    /**
     * 切片时长步长（后端 key: sliceStep），单位秒。
     */
    public static double getSliceDurationStep(Context context) {
        if (context == null) return 0.1;
        return parseDoubleFromString(SharedPrefUtil.getString(context, SP_KEY_GLOBAL_SLICE_STEP, ""), 0.1);
    }

    /**
     * 默认分镜头时长，按后端区间与步长约束后的默认值。
     */
    public static double getDefaultSliceDuration(Context context) {
        double min = getSliceDurationMin(context);
        double max = getSliceDurationMax(context);
        double step = getSliceDurationStep(context);
        double duration = 2.0;
        if (duration < min) duration = min;
        if (duration > max) duration = max;
        if (step > 0) {
            long steps = Math.round(duration / step);
            duration = steps * step;
            if (duration < min) duration = min;
            if (duration > max) duration = max;
        }
        return duration;
    }

    /**
     * 客服服务时段（后端 key: customerServiceHours）。
     */
    public static String getCustomerServiceHours(Context context) {
        if (context == null) return "";
        return SharedPrefUtil.getString(context, SP_KEY_GLOBAL_CUSTOMER_SERVICE_HOURS, "");
    }

    /**
     * 空状态占位图 URL（后端 key: emptyPlaceholder）。
     */
    public static String getEmptyPlaceholder(Context context) {
        if (context == null) return "";
        return SharedPrefUtil.getString(context, SP_KEY_GLOBAL_EMPTY_PLACEHOLDER, "");
    }

    private static String getValue(Map<String, Object> config, String key) {
        Object value = config.get(key);
        return value != null ? value.toString() : "";
    }

    private static int parseInt(Object value, int fallback) {
        if (value == null) return fallback;
        try {
            if (value instanceof Number) return ((Number) value).intValue();
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double parseDouble(Object value, double fallback) {
        if (value == null) return fallback;
        try {
            if (value instanceof Number) return ((Number) value).doubleValue();
            return Double.parseDouble(value.toString().trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double parseDoubleFromString(String value, double fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
