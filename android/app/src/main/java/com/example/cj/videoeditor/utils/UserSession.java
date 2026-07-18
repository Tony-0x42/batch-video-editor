package com.example.cj.videoeditor.utils;

import android.content.Context;
import android.text.TextUtils;

import com.example.cj.videoeditor.bean.User;
import com.example.cj.videoeditor.network.dto.BatchCustomerDto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserSession {

    private UserSession() {}

    public static void saveLogin(Context context, User user) {
        if (user == null) return;
        SharedPrefUtil.putBoolean(context, AppConfig.SP_KEY_IS_LOGIN, true);
        SharedPrefUtil.putString(context, AppConfig.SP_KEY_USER_PHONE, user.getPhone());
        SharedPrefUtil.putString(context, AppConfig.SP_KEY_USER_NAME, user.getNickname());
        SharedPrefUtil.putString(context, AppConfig.SP_KEY_USER_AVATAR, user.getAvatar());
        SharedPrefUtil.putBoolean(context, AppConfig.SP_KEY_USER_VIP, user.isVip());
        SharedPrefUtil.putString(context, AppConfig.SP_KEY_USER_VIP_EXPIRE, user.getVipExpire());
        SharedPrefUtil.putInt(context, AppConfig.SP_KEY_COMPUTE_TOTAL, Math.max(user.getComputePower(), 0));
        SharedPrefUtil.putInt(context, AppConfig.SP_KEY_COMPUTE_USED, user.getUsedComputePower());
    }

    /**
     * 保存 APP 登录/注册后返回的客户信息。
     */
    public static void saveLogin(Context context, BatchCustomerDto customer) {
        if (customer == null) return;

        String phone = customer.getPhone() != null ? customer.getPhone() : "";
        String name = !TextUtils.isEmpty(customer.getCustomerName())
                ? customer.getCustomerName() : phone;
        String avatar = customer.getAvatarUrl() != null ? customer.getAvatarUrl() : "";
        boolean vip = isVipValid(customer);

        SharedPrefUtil.putBoolean(context, AppConfig.SP_KEY_IS_LOGIN, true);
        SharedPrefUtil.putString(context, AppConfig.SP_KEY_CUSTOMER_ID,
                customer.getCustomerId() != null ? String.valueOf(customer.getCustomerId()) : "");
        SharedPrefUtil.putString(context, AppConfig.SP_KEY_USER_PHONE, phone);
        SharedPrefUtil.putString(context, AppConfig.SP_KEY_USER_NAME, name);
        SharedPrefUtil.putString(context, AppConfig.SP_KEY_USER_AVATAR, avatar);
        SharedPrefUtil.putBoolean(context, AppConfig.SP_KEY_USER_VIP, vip);
        SharedPrefUtil.putString(context, AppConfig.SP_KEY_USER_VIP_EXPIRE,
                customer.getVipExpireDate() != null ? customer.getVipExpireDate() : "");
        SharedPrefUtil.putInt(context, AppConfig.SP_KEY_COMPUTE_TOTAL, toInt(customer.getComputingPowerTotal()));
        SharedPrefUtil.putInt(context, AppConfig.SP_KEY_COMPUTE_USED, toInt(customer.getComputingPowerUsed()));
        SharedPrefUtil.putInt(context, AppConfig.SP_KEY_COMPUTE_REMAIN, toInt(customer.getComputingPowerRemain()));
        SharedPrefUtil.putInt(context, AppConfig.SP_KEY_CUSTOMER_TYPE,
                customer.getCustomerType() != null ? customer.getCustomerType() : 3);
        SharedPrefUtil.putString(context, AppConfig.SP_KEY_PARENT_PHONE,
                customer.getParentPhone() != null ? customer.getParentPhone() : "");
    }

    public static boolean isLogin(Context context) {
        return SharedPrefUtil.getBoolean(context, AppConfig.SP_KEY_IS_LOGIN, false);
    }

    public static void logout(Context context) {
        SharedPrefUtil.clear(context);
    }

    private static int toInt(Double value) {
        if (value == null) return 0;
        return value.intValue();
    }

    private static boolean isVipValid(BatchCustomerDto customer) {
        if (customer.getVipStatus() != null) {
            return customer.getVipStatus() == 0;
        }
        String expire = customer.getVipExpireDate();
        if (TextUtils.isEmpty(expire)) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expireDate = sdf.parse(expire.split(" ")[0]);
            Date today = sdf.parse(sdf.format(new Date()));
            return expireDate != null && !expireDate.before(today);
        } catch (Exception e) {
            return false;
        }
    }
}
