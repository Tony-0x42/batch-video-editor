package com.example.cj.videoeditor.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.bean.VideoClip;
import com.example.cj.videoeditor.network.ApiCallback;
import com.example.cj.videoeditor.network.ApiService;
import com.example.cj.videoeditor.network.dto.BatchAiVideoClipDto;
import com.example.cj.videoeditor.network.dto.BatchAiVideoGenerateBody;
import com.example.cj.videoeditor.network.dto.BatchAiVideoGenerateResultDto;
import com.example.cj.videoeditor.network.dto.BatchAiVideoGroupDto;
import com.example.cj.videoeditor.network.dto.BatchCustomerDto;
import com.example.cj.videoeditor.utils.AppConfig;
import com.example.cj.videoeditor.utils.SharedPrefUtil;
import com.example.cj.videoeditor.utils.ToastUtil;
import com.example.cj.videoeditor.utils.UserStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AiCreationEditActivity extends BaseActivity {

    @Inject
    ApiService apiService;

    private EditText etGroupName;
    private RecyclerView recyclerView;
    private Button btnAddClip;
    private Button btnGenerate;
    private ProgressBar loadingProgress;
    private ClipAdapter adapter;
    private List<VideoClip> clipList = new ArrayList<>();

    private Long groupId;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_ai_creation_edit;
    }

    @Override
    protected void initViews() {
        setTitle(getString(R.string.edit_group));
        groupId = getIntent().getLongExtra("group_id", -1L);
        String groupName = getIntent().getStringExtra("group_name");

        etGroupName = findViewById(R.id.et_group_name);
        recyclerView = findViewById(R.id.recycler_view);
        btnAddClip = findViewById(R.id.btn_add_clip);
        btnGenerate = findViewById(R.id.btn_generate);
        loadingProgress = findViewById(R.id.loading_progress);

        etGroupName.setText(groupName == null ? "" : groupName);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClipAdapter();
        recyclerView.setAdapter(adapter);

        if (groupId != null && groupId > 0) {
            loadGroupDetail(groupId);
        } else {
            clipList.add(new VideoClip(String.valueOf(System.currentTimeMillis()), "", "分镜头 1", AppConfig.getDefaultSliceDuration(this)));
            adapter.notifyDataSetChanged();
        }

        btnAddClip.setOnClickListener(v -> {
            int maxClips = AppConfig.getMaxVideos(this);
            if (clipList.size() >= maxClips) {
                ToastUtil.show(this, String.format(Locale.getDefault(), "最多 %d 个分镜头", maxClips));
                return;
            }
            clipList.add(new VideoClip(String.valueOf(System.currentTimeMillis()), "", "分镜头 " + (clipList.size() + 1), AppConfig.getDefaultSliceDuration(this)));
            adapter.notifyDataSetChanged();
        });

        btnGenerate.setOnClickListener(v -> attemptGenerate());
    }

    private void loadGroupDetail(Long groupId) {
        showLoading(true);
        apiService.getAiVideoGroup(groupId).enqueue(new ApiCallback<BatchAiVideoGroupDto>() {
            @Override
            public void onSuccess(BatchAiVideoGroupDto data) {
                showLoading(false);
                if (data == null) {
                    addDefaultClip();
                    return;
                }
                if (data.getGroupName() != null) {
                    etGroupName.setText(data.getGroupName());
                }
                clipList.clear();
                List<BatchAiVideoClipDto> clips = data.getClips();
                if (clips != null && !clips.isEmpty()) {
                    for (BatchAiVideoClipDto c : clips) {
                        String id = c.getClipId() != null ? String.valueOf(c.getClipId()) : String.valueOf(System.currentTimeMillis());
                        String text = c.getTextContent() != null ? c.getTextContent() : "";
                        double duration = c.getDuration() != null ? c.getDuration() : 2.0;
                        clipList.add(new VideoClip(id, c.getVideoUrl(), text, duration));
                    }
                } else {
                    addDefaultClip();
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String msg) {
                showLoading(false);
                ToastUtil.show(AiCreationEditActivity.this, msg);
                addDefaultClip();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void addDefaultClip() {
        if (clipList.isEmpty()) {
            clipList.add(new VideoClip(String.valueOf(System.currentTimeMillis()), "", "分镜头 1", AppConfig.getDefaultSliceDuration(this)));
        }
    }

    private void attemptGenerate() {
        int total = SharedPrefUtil.getInt(this, AppConfig.SP_KEY_COMPUTE_TOTAL, 0);
        int used = SharedPrefUtil.getInt(this, AppConfig.SP_KEY_COMPUTE_USED, 0);
        if (used >= total) {
            new AlertDialog.Builder(this)
                    .setMessage(AppConfig.getPowerExhaustedMessage(this))
                    .setPositiveButton(R.string.confirm, null)
                    .show();
            return;
        }

        if (groupId == null || groupId <= 0) {
            ToastUtil.show(this, "视频组信息异常");
            return;
        }

        saveGroupAndGenerate();
    }

    private void saveGroupAndGenerate() {
        BatchAiVideoGroupDto group = new BatchAiVideoGroupDto();
        group.setGroupId(groupId);
        group.setGroupName(etGroupName.getText().toString().trim());
        group.setClips(buildClipDtos());

        showLoading(true);
        apiService.updateAiVideoGroup(group).enqueue(new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object data) {
                submitGenerate();
            }

            @Override
            public void onError(String msg) {
                showLoading(false);
                ToastUtil.show(AiCreationEditActivity.this, msg);
            }
        });
    }

    private void submitGenerate() {
        BatchAiVideoGenerateBody body = new BatchAiVideoGenerateBody(groupId, 1, buildClipDtos());
        apiService.generateAiVideo(body).enqueue(new ApiCallback<BatchAiVideoGenerateResultDto>() {
            @Override
            public void onSuccess(BatchAiVideoGenerateResultDto data) {
                showLoading(false);
                refreshCustomerInfo();
                ToastUtil.show(AiCreationEditActivity.this, "开始 AI 生成");
                startActivity(new Intent(AiCreationEditActivity.this, SplitProgressActivity.class));
            }

            @Override
            public void onError(String msg) {
                showLoading(false);
                ToastUtil.show(AiCreationEditActivity.this, msg);
            }
        });
    }

    private List<BatchAiVideoClipDto> buildClipDtos() {
        List<BatchAiVideoClipDto> list = new ArrayList<>();
        for (int i = 0; i < clipList.size(); i++) {
            VideoClip clip = clipList.get(i);
            BatchAiVideoClipDto dto = new BatchAiVideoClipDto();
            dto.setGroupId(groupId);
            try {
                dto.setClipId(clip.getId() != null ? Long.parseLong(clip.getId()) : null);
            } catch (NumberFormatException e) {
                dto.setClipId(null);
            }
            dto.setVideoUrl(clip.getVideoUrl());
            dto.setTextContent(clip.getText());
            dto.setDuration(clip.getDuration());
            dto.setSortOrder(i);
            list.add(dto);
        }
        return list;
    }

    private void refreshCustomerInfo() {
        String phone = SharedPrefUtil.getString(this, AppConfig.SP_KEY_USER_PHONE, "");
        if (phone.isEmpty()) {
            return;
        }
        apiService.getAppCustomer(phone).enqueue(new ApiCallback<BatchCustomerDto>() {
            @Override
            public void onSuccess(BatchCustomerDto data) {
                if (data != null) {
                    UserStore.saveCustomerDto(AiCreationEditActivity.this, data);
                }
            }

            @Override
            public void onError(String msg) {
                // 静默失败
            }
        });
    }

    private void showLoading(boolean show) {
        if (loadingProgress != null) {
            loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private class ClipAdapter extends RecyclerView.Adapter<ClipAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clip, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            VideoClip clip = clipList.get(position);
            holder.etText.setText(clip.getText());
            holder.tvDuration.setText(String.format(Locale.getDefault(), "时长：%.1fs", clip.getDuration()));
            holder.etText.setTag(position);
            holder.etText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    Object tag = holder.etText.getTag();
                    if (tag != null) {
                        int pos = (int) tag;
                        if (pos < clipList.size()) {
                            clipList.get(pos).setText(s.toString());
                        }
                    }
                }
            });
            holder.ivDelete.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos >= 0 && pos < clipList.size()) {
                    clipList.remove(pos);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return clipList.size();
        }

        class VH extends RecyclerView.ViewHolder {
            EditText etText;
            TextView tvDuration;
            ImageView ivDelete;
            VH(View itemView) {
                super(itemView);
                etText = itemView.findViewById(R.id.et_text);
                tvDuration = itemView.findViewById(R.id.tv_duration);
                ivDelete = itemView.findViewById(R.id.iv_delete);
            }
        }
    }
}
