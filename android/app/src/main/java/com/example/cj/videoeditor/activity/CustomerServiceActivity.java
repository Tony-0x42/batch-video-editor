package com.example.cj.videoeditor.activity;

import dagger.hilt.android.AndroidEntryPoint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.network.ApiCallback;
import com.example.cj.videoeditor.network.ApiService;
import com.example.cj.videoeditor.network.dto.BatchContactDto;
import com.example.cj.videoeditor.utils.AppConfig;
import com.example.cj.videoeditor.utils.ToastUtil;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

@AndroidEntryPoint
public class CustomerServiceActivity extends BaseActivity {

    @Inject
    ApiService apiService;

    private TextView tvPhone;
    private TextView tvTime;
    private Button btnCall;

    private String servicePhone = "";
    private String serviceHours = "";
    private int loadedCount = 0;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_customer_service;
    }

    @Override
    protected void initViews() {
        setTitle(getString(R.string.customer_service));
        tvPhone = findViewById(R.id.tv_phone);
        tvTime = findViewById(R.id.tv_time);
        btnCall = findViewById(R.id.btn_call);

        bindData();
        btnCall.setOnClickListener(v -> callService());
    }

    @Override
    protected void initData() {
        loadGlobalConfig();
        loadContactList();
    }

    private void loadGlobalConfig() {
        apiService.getGlobalConfig().enqueue(new ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                AppConfig.saveGlobalConfig(CustomerServiceActivity.this, data);
                serviceHours = AppConfig.getCustomerServiceHours(CustomerServiceActivity.this);
                onDataLoaded();
            }

            @Override
            public void onError(String msg) {
                serviceHours = AppConfig.getCustomerServiceHours(CustomerServiceActivity.this);
                onDataLoaded();
            }
        });
    }

    private void loadContactList() {
        apiService.getContactList().enqueue(new ApiCallback<List<BatchContactDto>>() {
            @Override
            public void onSuccess(List<BatchContactDto> data) {
                if (data != null) {
                    for (BatchContactDto dto : data) {
                        if (dto.getContactType() != null && dto.getContactType() == 1) {
                            servicePhone = dto.getPhone() != null ? dto.getPhone() : "";
                            break;
                        }
                    }
                }
                onDataLoaded();
            }

            @Override
            public void onError(String msg) {
                onDataLoaded();
            }
        });
    }

    private synchronized void onDataLoaded() {
        loadedCount++;
        if (loadedCount >= 2) {
            bindData();
        }
    }

    private void bindData() {
        tvPhone.setText(!servicePhone.isEmpty() ? servicePhone : "暂未配置");
        tvTime.setText(!serviceHours.isEmpty() ? serviceHours : "暂未配置");
    }

    private void callService() {
        if (servicePhone == null || servicePhone.isEmpty()) {
            ToastUtil.show(this, "暂未配置客服电话");
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + servicePhone));
            startActivity(intent);
        } catch (Exception e) {
            ToastUtil.show(this, "无法拨打电话");
        }
    }
}
