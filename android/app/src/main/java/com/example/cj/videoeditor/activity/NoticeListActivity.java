package com.example.cj.videoeditor.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.adapter.NoticeAdapter;
import com.example.cj.videoeditor.network.ApiCallback;
import com.example.cj.videoeditor.network.ApiService;
import com.example.cj.videoeditor.network.PageApiCallback;
import com.example.cj.videoeditor.network.dto.BatchAppNoticeDto;
import com.example.cj.videoeditor.utils.ToastUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NoticeListActivity extends BaseActivity {

    @Inject
    ApiService apiService;

    private RecyclerView rvNotices;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_notice_list;
    }

    @Override
    protected void initViews() {
        setTitle(getString(R.string.notice_title));
        rvNotices = findViewById(R.id.rv_notices);
        tvEmpty = findViewById(R.id.tv_empty);
        progressBar = findViewById(R.id.progress_bar);
        rvNotices.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void initData() {
        loadNotices();
    }

    private void loadNotices() {
        showLoading(true);
        Map<String, String> params = new HashMap<>();
        params.put("publishStatus", "0");
        params.put("pageNum", "1");
        params.put("pageSize", "1000");
        apiService.getNoticeList(params).enqueue(new PageApiCallback<BatchAppNoticeDto>() {
            @Override
            public void onSuccess(long total, List<BatchAppNoticeDto> rows) {
                showLoading(false);
                List<BatchAppNoticeDto> list = rows != null ? rows : new ArrayList<>();
                rvNotices.setAdapter(new NoticeAdapter(list, notice -> {
                    if (notice != null && notice.getNoticeId() != null) {
                        loadNoticeDetail(notice.getNoticeId());
                    }
                }));
                tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                rvNotices.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(String msg) {
                showLoading(false);
                ToastUtil.show(NoticeListActivity.this, getString(R.string.notice_load_failed, msg));
                rvNotices.setAdapter(new NoticeAdapter(new ArrayList<>(), null));
                tvEmpty.setVisibility(View.VISIBLE);
                rvNotices.setVisibility(View.GONE);
            }
        });
    }

    private void loadNoticeDetail(Long noticeId) {
        showLoading(true);
        apiService.getNoticeById(noticeId).enqueue(new ApiCallback<BatchAppNoticeDto>() {
            @Override
            public void onSuccess(BatchAppNoticeDto data) {
                showLoading(false);
                if (data == null) {
                    ToastUtil.show(NoticeListActivity.this, R.string.notice_not_exist);
                    return;
                }
                String title = data.getNoticeTitle() != null ? data.getNoticeTitle() : "";
                String content = data.getContent() != null ? data.getContent() : "";
                new androidx.appcompat.app.AlertDialog.Builder(NoticeListActivity.this)
                        .setTitle(title)
                        .setMessage(content)
                        .setPositiveButton(R.string.confirm, null)
                        .show();
            }

            @Override
            public void onError(String msg) {
                showLoading(false);
                ToastUtil.show(NoticeListActivity.this, getString(R.string.notice_load_failed, msg));
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
