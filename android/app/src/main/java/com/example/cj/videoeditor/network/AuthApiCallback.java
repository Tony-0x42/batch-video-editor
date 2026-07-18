package com.example.cj.videoeditor.network;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 认证类接口专用回调，同时暴露 wrapper 层的 token 与 data。
 *
 * <p>适用于 /batch/app/login、/batch/app/register 等返回 {@code BaseResponse<T>}
 * 且 token 位于 wrapper 层（而非 data 内）的接口。</p>
 */
public abstract class AuthApiCallback<T> implements Callback<BaseResponse<T>> {

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
                onSuccess(body.getToken(), body.getData());
            } else {
                onError(body.getMsg() != null ? body.getMsg() : "请求失败");
            }
        } else {
            onError("请求失败：" + response.code());
        }
    }

    @Override
    public void onFailure(Call<BaseResponse<T>> call, Throwable t) {
        onError(t.getMessage() != null ? t.getMessage() : "网络请求失败");
    }

    /**
     * 业务成功时回调。
     *
     * @param token 登录/注册成功后后端返回的 JWT token；非认证接口可能为 null
     * @param data  业务数据，可能为 null
     */
    public abstract void onSuccess(String token, T data);

    /**
     * 业务失败或网络异常时回调。
     */
    public abstract void onError(String msg);
}
