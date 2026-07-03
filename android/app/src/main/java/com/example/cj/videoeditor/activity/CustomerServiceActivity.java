package com.example.cj.videoeditor.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.utils.ToastUtil;

public class CustomerServiceActivity extends BaseActivity {

    private TextView tvPhone;
    private TextView tvTime;
    private Button btnCall;

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

        tvPhone.setText("400-888-8888");
        tvTime.setText("早 9:00-18:00");

        btnCall.setOnClickListener(v -> callService());
    }

    private void callService() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:400-888-8888"));
            startActivity(intent);
        } catch (Exception e) {
            ToastUtil.show(this, "无法拨打电话");
        }
    }
}
