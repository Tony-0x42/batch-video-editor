package com.example.cj.videoeditor.network;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 简化版 Retrofit 回调，自动解析 {@link BaseResponse} 并提取 data。
 *
 * 适用于返回 {@code BaseResponse<T>} 的接口。
 */
public abstract class ApiCallback<T> implements Callback<BaseResponse<T>> {

    @Override
    public void onResponse(Call<BaseResponse<T>> call, Response<BaseResponse<T>> response) {
        Integer bizCode = response.body() != null ? response.body().getCode() : null;
        if (SessionExpiredHandler.isSessionExpired(response.code(), bizCode)) {
            SessionExpiredHandler.handleSessionExpired();
            return;
        }
        if (response.isSuccessful() && response.body() != null) {
            BaseResponse<T> body = response.body();
            if (body.isSuccess()) {
                onSuccess(body.getData());
            } else {
                onError(body.getMsg() != null ? body.getMsg() : "请求失败");
            }
        } else {
            onError("请求失败：" + response.code());
        }
    }

    @Override
    public void onFailure(Call<BaseResponse<T>> call, Throwable t) {
        onError("网络请求失败，请检查网络连接后重试");
    }

    /**
     * 业务成功时回调，data 可能为 null（如后端返回空 data）。
     */
    public abstract void onSuccess(T data);

    /**
     * 业务失败或网络异常时回调。
     */
    public abstract void onError(String msg);
}
