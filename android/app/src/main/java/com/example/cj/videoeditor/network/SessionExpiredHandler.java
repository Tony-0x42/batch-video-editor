package com.example.cj.videoeditor.network;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.cj.videoeditor.MyApplication;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.activity.LoginActivity;
import com.example.cj.videoeditor.utils.UserSession;

/**
 * 登录态过期（401）统一处理：清除本地登录态并跳转登录页。
 *
 * 回调可能发生在任意线程，因此统一切换到主线程执行 Toast 与跳转，
 * 并使用全局 Application Context 发起跳转。
 */
public final class SessionExpiredHandler {

    /**
     * 去抖间隔，避免并发请求同时 401 时重复跳转登录页。
     */
    private static final long DEBOUNCE_MS = 3000L;

    private static long lastHandledAt = 0L;

    private SessionExpiredHandler() {}

    /**
     * 判断响应是否属于登录态过期（HTTP 401 或业务 code 401）。
     */
    public static boolean isSessionExpired(int httpCode, Integer bizCode) {
        return httpCode == 401 || (bizCode != null && bizCode == 401);
    }

    /**
     * 清除本地登录态，Toast 提示并跳转登录页。
     */
    public static void handleSessionExpired() {
        long now = System.currentTimeMillis();
        synchronized (SessionExpiredHandler.class) {
            if (now - lastHandledAt < DEBOUNCE_MS) {
                return;
            }
            lastHandledAt = now;
        }
        Context context = MyApplication.getContext();
        if (context == null) {
            return;
        }
        // 清除登录态（含 token、用户信息、算力缓存等全部 SP 数据）
        UserSession.logout(context);
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(context, R.string.login_expired, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
    }
}
