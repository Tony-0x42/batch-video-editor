package com.example.cj.videoeditor.network;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

/**
 * 分页接口专用回调，自动解析 {@link PageResponse} 并提取 rows。
 */
public abstract class PageApiCallback<T> implements Callback<PageResponse<T>> {

    @Override
    public void onResponse(Call<PageResponse<T>> call, Response<PageResponse<T>> response) {
        Integer bizCode = response.body() != null ? response.body().getCode() : null;
        if (SessionExpiredHandler.isSessionExpired(response.code(), bizCode)) {
            SessionExpiredHandler.handleSessionExpired();
            return;
        }
        if (response.isSuccessful() && response.body() != null) {
            PageResponse<T> body = response.body();
            if (body.isSuccess()) {
                onSuccess(body.getTotal(), body.getSafeRows());
            } else {
                onError(body.getMsg() != null ? body.getMsg() : "请求失败");
            }
        } else {
            onError("请求失败：" + response.code());
        }
    }

    @Override
    public void onFailure(Call<PageResponse<T>> call, Throwable t) {
        onError(t.getMessage() != null ? t.getMessage() : "网络请求失败");
    }

    /**
     * 业务成功时回调。
     *
     * @param total 总记录数
     * @param rows  当前页数据（不会为 null）
     */
    public abstract void onSuccess(long total, List<T> rows);

    /**
     * 业务失败或网络异常时回调。
     */
    public abstract void onError(String msg);
}
