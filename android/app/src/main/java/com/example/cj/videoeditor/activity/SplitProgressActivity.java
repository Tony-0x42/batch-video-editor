package com.example.cj.videoeditor.activity;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.network.ApiCallback;
import com.example.cj.videoeditor.network.ApiService;
import com.example.cj.videoeditor.network.RetrofitClient;
import com.example.cj.videoeditor.utils.DownloadUtil;
import com.example.cj.videoeditor.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplitProgressActivity extends BaseActivity {

    private static final long POLL_INTERVAL_MS = 2000L;

    @Inject
    ApiService apiService;

    private ProgressBar progressBar;
    private TextView tvPercent;
    private TextView tvStatus;
    private TextView tvEmpty;
    private RecyclerView recyclerTasks;
    private Button btnCancel;
    private TaskAdapter adapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<TaskItem> tasks = new ArrayList<>();
    private Runnable pollRunnable;
    private long groupId = -1L;

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
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerTasks = findViewById(R.id.recycler_tasks);
        btnCancel = findViewById(R.id.btn_cancel);

        groupId = getIntent().getLongExtra("group_id", -1L);

        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter();
        recyclerTasks.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> finish());

        if (groupId <= 0) {
            tvStatus.setText("任务参数异常");
            updateEmptyView();
        } else {
            startPolling();
        }
    }

    private void startPolling() {
        pollRunnable = this::fetchTasks;
        handler.post(pollRunnable);
    }

    private void fetchTasks() {
        apiService.getAiVideoTaskList(groupId).enqueue(new ApiCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> list) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                tasks.clear();
                if (list != null) {
                    for (Map<String, Object> map : list) {
                        tasks.add(TaskItem.fromMap(map));
                    }
                }
                adapter.notifyDataSetChanged();
                updateSummary();
                updateEmptyView();
                if (hasRunningTask()) {
                    scheduleNextPoll();
                }
            }

            @Override
            public void onError(String msg) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                tvStatus.setText("进度获取失败，2 秒后重试…");
                scheduleNextPoll();
            }
        });
    }

    private void scheduleNextPoll() {
        if (pollRunnable != null && !isFinishing() && !isDestroyed()) {
            handler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
        }
    }

    private boolean hasRunningTask() {
        for (TaskItem item : tasks) {
            if (item.status == 0) {
                return true;
            }
        }
        return false;
    }

    private void updateSummary() {
        int total = tasks.size();
        int running = 0;
        int success = 0;
        int failed = 0;
        int progressSum = 0;
        for (TaskItem item : tasks) {
            if (item.status == 0) {
                running++;
            } else if (item.status == 1) {
                success++;
            } else if (item.status == 2) {
                failed++;
            }
            progressSum += item.progress;
        }
        int overall = total == 0 ? 0 : progressSum / total;
        progressBar.setProgress(overall);
        tvPercent.setText(String.format(Locale.getDefault(), "%d%%", overall));

        if (total == 0) {
            tvStatus.setText("暂无生成任务");
        } else if (running > 0) {
            tvStatus.setText(String.format(Locale.getDefault(), "正在生成（已完成 %d/%d）…", success + failed, total));
        } else {
            tvStatus.setText(String.format(Locale.getDefault(), "生成完成：成功 %d 个，失败 %d 个", success, failed));
        }
    }

    private void updateEmptyView() {
        boolean empty = tasks.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerTasks.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private String resolveUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        String base = RetrofitClient.INSTANCE.createRetrofit(this).baseUrl().toString();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return url.startsWith("/") ? base + url : base + "/" + url;
    }

    private void saveVideo(TaskItem item) {
        String url = resolveUrl(item.resultUrl);
        if (TextUtils.isEmpty(url)) {
            ToastUtil.show(this, "视频地址为空");
            return;
        }
        ToastUtil.show(this, R.string.download_start);
        DownloadUtil.downloadVideo(this, url, (success, localUri) ->
                ToastUtil.show(SplitProgressActivity.this,
                        success ? R.string.download_success : R.string.download_failed));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pollRunnable != null) {
            handler.removeCallbacks(pollRunnable);
        }
    }

    /**
     * 单个生成任务（status: 0=处理中 1=成功 2=失败）。
     */
    private static class TaskItem {
        long logId;
        int status;
        int progress;
        String resultUrl;
        String errorMsg;
        String createTime;

        static TaskItem fromMap(Map<String, Object> map) {
            TaskItem item = new TaskItem();
            if (map == null) {
                return item;
            }
            item.logId = toLong(map.get("logId"));
            item.status = (int) toLong(map.get("status"));
            item.progress = (int) toLong(map.get("progress"));
            Object resultUrl = map.get("resultUrl");
            item.resultUrl = resultUrl != null ? resultUrl.toString() : "";
            Object errorMsg = map.get("errorMsg");
            item.errorMsg = errorMsg != null ? errorMsg.toString() : "";
            Object createTime = map.get("createTime");
            item.createTime = createTime != null ? createTime.toString() : "";
            return item;
        }

        private static long toLong(Object value) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            if (value != null) {
                try {
                    return Long.parseLong(value.toString());
                } catch (NumberFormatException ignored) {
                }
            }
            return 0L;
        }
    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ai_task, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            TaskItem item = tasks.get(position);
            holder.tvTaskName.setText(String.format(Locale.getDefault(), "视频 %d", position + 1));
            holder.progressTask.setProgress(item.progress);

            holder.tvError.setVisibility(View.GONE);
            holder.btnSave.setVisibility(View.GONE);
            if (item.status == 0) {
                holder.tvTaskStatus.setText(String.format(Locale.getDefault(), "处理中 %d%%", item.progress));
                holder.tvTaskStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else if (item.status == 1) {
                holder.tvTaskStatus.setText("已完成");
                holder.tvTaskStatus.setTextColor(getResources().getColor(R.color.success));
                if (!TextUtils.isEmpty(item.resultUrl)) {
                    holder.btnSave.setVisibility(View.VISIBLE);
                }
            } else {
                holder.tvTaskStatus.setText("生成失败");
                holder.tvTaskStatus.setTextColor(getResources().getColor(R.color.error));
                holder.tvError.setVisibility(View.VISIBLE);
                holder.tvError.setText(TextUtils.isEmpty(item.errorMsg) ? "未知错误" : item.errorMsg);
            }

            holder.btnSave.setOnClickListener(v -> saveVideo(item));
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTaskName;
            TextView tvTaskStatus;
            ProgressBar progressTask;
            TextView tvError;
            Button btnSave;

            VH(View itemView) {
                super(itemView);
                tvTaskName = itemView.findViewById(R.id.tv_task_name);
                tvTaskStatus = itemView.findViewById(R.id.tv_task_status);
                progressTask = itemView.findViewById(R.id.progress_task);
                tvError = itemView.findViewById(R.id.tv_error);
                btnSave = itemView.findViewById(R.id.btn_save);
            }
        }
    }
}
