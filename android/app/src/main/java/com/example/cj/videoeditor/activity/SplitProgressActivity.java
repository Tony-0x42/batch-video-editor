package com.example.cj.videoeditor.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cj.videoeditor.R;

public class SplitProgressActivity extends BaseActivity {

    private ProgressBar progressBar;
    private TextView tvPercent;
    private TextView tvStatus;
    private Button btnCancel;

    private Handler handler = new Handler(Looper.getMainLooper());
    private int progress = 0;
    private Runnable progressRunnable;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_split_progress;
    }

    @Override
    protected void initViews() {
        setTitle(getString(R.string.split_progress));
        progressBar = findViewById(R.id.progress_bar);
        tvPercent = findViewById(R.id.tv_percent);
        tvStatus = findViewById(R.id.tv_status);
        btnCancel = findViewById(R.id.btn_cancel);

        btnCancel.setOnClickListener(v -> finish());
        startProgress();
    }

    private void startProgress() {
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                progress += 5;
                if (progress > 100) progress = 100;
                progressBar.setProgress(progress);
                tvPercent.setText(String.format(java.util.Locale.getDefault(), "%d%%", progress));
                if (progress < 100) {
                    handler.postDelayed(this, 200);
                } else {
                    tvStatus.setText("分割完成");
                    handler.postDelayed(() -> finish(), 500);
                }
            }
        };
        handler.postDelayed(progressRunnable, 200);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressRunnable != null) {
            handler.removeCallbacks(progressRunnable);
        }
    }
}
