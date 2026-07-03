package com.example.cj.videoeditor.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cj.videoeditor.R;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public abstract class BaseActivity extends AppCompatActivity {

    private TextView tvTitle;
    private ImageView ivBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        initTitleBar();
        ViewGroup container = findViewById(R.id.content_container);
        if (container != null && getContentLayoutId() != 0) {
            getLayoutInflater().inflate(getContentLayoutId(), container, true);
        }
        initViews();
        initData();
    }

    @LayoutRes
    protected abstract int getContentLayoutId();

    protected abstract void initViews();

    protected void initData() {}

    private void initTitleBar() {
        tvTitle = findViewById(R.id.tv_title);
        ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
    }

    protected void setTitle(String title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    protected void hideBackButton() {
        if (ivBack != null) {
            ivBack.setVisibility(View.GONE);
        }
    }
}
