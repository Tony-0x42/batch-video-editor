package com.example.cj.videoeditor.activity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.bean.VideoClip;
import com.example.cj.videoeditor.network.ApiCallback;
import com.example.cj.videoeditor.network.ApiService;
import com.example.cj.videoeditor.network.dto.BatchAiVideoClipDto;
import com.example.cj.videoeditor.network.dto.BatchAiVideoGroupDto;
import com.example.cj.videoeditor.network.dto.BatchCustomerDto;
import com.example.cj.videoeditor.utils.AppConfig;
import com.example.cj.videoeditor.utils.SharedPrefUtil;
import com.example.cj.videoeditor.utils.ToastUtil;
import com.example.cj.videoeditor.utils.UserStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

@AndroidEntryPoint
public class AiCreationEditActivity extends BaseActivity {

    private static final double DEFAULT_SLICE_DURATION = 3.0;
    private static final int DEFAULT_GENERATE_COUNT = 3;
    private static final int MAX_GENERATE_COUNT = 10;

    @Inject
    ApiService apiService;

    private EditText etGroupName;
    private RecyclerView recyclerView;
    private TextView tvEmptyClips;
    private Button btnPickVideo;
    private Button btnGenerate;
    private ProgressBar loadingProgress;
    private TextView tvUploadStatus;
    private SeekBar seekSliceDuration;
    private TextView tvSliceDuration;
    private TextView tvGenerateCount;
    private ClipAdapter adapter;
    private List<VideoClip> clipList = new ArrayList<>();

    private Long groupId;
    private double sliceMin;
    private double sliceStep;
    private double sliceDuration = DEFAULT_SLICE_DURATION;
    private int generateCount = DEFAULT_GENERATE_COUNT;
    private boolean uploading = false;

    private final ActivityResultLauncher<Intent> videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    handlePickedVideos(result.getData());
                }
            }
    );

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
        tvEmptyClips = findViewById(R.id.tv_empty_clips);
        btnPickVideo = findViewById(R.id.btn_pick_video);
        btnGenerate = findViewById(R.id.btn_generate);
        loadingProgress = findViewById(R.id.loading_progress);
        tvUploadStatus = findViewById(R.id.tv_upload_status);
        seekSliceDuration = findViewById(R.id.seek_slice_duration);
        tvSliceDuration = findViewById(R.id.tv_slice_duration);
        tvGenerateCount = findViewById(R.id.tv_generate_count);

        etGroupName.setText(groupName == null ? "" : groupName);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClipAdapter();
        recyclerView.setAdapter(adapter);

        setupSliceDurationSeekBar();
        updateGenerateCountText();

        if (groupId != null && groupId > 0) {
            loadGroupDetail(groupId);
        }
        updateEmptyView();

        btnPickVideo.setOnClickListener(v -> pickVideos());
        tvGenerateCount.setOnClickListener(v -> showGenerateCountDialog());
        btnGenerate.setOnClickListener(v -> attemptGenerate());
    }

    private void setupSliceDurationSeekBar() {
        sliceMin = AppConfig.getSliceDurationMin(this);
        double sliceMax = AppConfig.getSliceDurationMax(this);
        sliceStep = AppConfig.getSliceDurationStep(this);
        if (sliceStep <= 0) {
            sliceStep = 0.1;
        }
        if (sliceMax <= sliceMin) {
            sliceMax = sliceMin + sliceStep;
        }
        int seekMax = (int) Math.round((sliceMax - sliceMin) / sliceStep);
        seekSliceDuration.setMax(Math.max(seekMax, 1));

        sliceDuration = DEFAULT_SLICE_DURATION;
        if (sliceDuration < sliceMin) {
            sliceDuration = sliceMin;
        }
        if (sliceDuration > sliceMax) {
            sliceDuration = sliceMax;
        }
        seekSliceDuration.setProgress(durationToProgress(sliceDuration));
        updateSliceDurationText();

        seekSliceDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sliceDuration = sliceMin + progress * sliceStep;
                // 按步长取整，避免浮点误差
                sliceDuration = Math.round(sliceDuration / sliceStep) * sliceStep;
                updateSliceDurationText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private int durationToProgress(double duration) {
        return (int) Math.round((duration - sliceMin) / sliceStep);
    }

    private void updateSliceDurationText() {
        tvSliceDuration.setText(String.format(Locale.getDefault(), "%.1f 秒", sliceDuration));
    }

    private void updateGenerateCountText() {
        tvGenerateCount.setText(String.format(Locale.getDefault(), "%d 个", generateCount));
    }

    private void showGenerateCountDialog() {
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(1);
        picker.setMaxValue(MAX_GENERATE_COUNT);
        picker.setValue(generateCount);
        new AlertDialog.Builder(this)
                .setTitle("生成数量")
                .setView(picker)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    generateCount = picker.getValue();
                    updateGenerateCountText();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void pickVideos() {
        if (uploading) {
            ToastUtil.show(this, "素材上传中，请稍候");
            return;
        }
        if (groupId == null || groupId <= 0) {
            ToastUtil.show(this, "视频组信息异常");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        videoPickerLauncher.launch(intent);
    }

    private void handlePickedVideos(Intent data) {
        List<Uri> uris = new ArrayList<>();
        ClipData clipData = data.getClipData();
        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                uris.add(clipData.getItemAt(i).getUri());
            }
        } else if (data.getData() != null) {
            uris.add(data.getData());
        }
        if (uris.isEmpty()) {
            return;
        }
        int maxVideos = AppConfig.getMaxVideos(this);
        if (uris.size() > maxVideos) {
            ToastUtil.show(this, String.format(Locale.getDefault(),
                    "单次最多选择 %d 个视频，已截取前 %d 个", maxVideos, maxVideos));
            uris = new ArrayList<>(uris.subList(0, maxVideos));
        }
        uploadNext(uris, 0);
    }

    private void uploadNext(List<Uri> uris, int index) {
        if (index >= uris.size()) {
            setUploading(false);
            tvUploadStatus.setText("素材处理完成");
            updateEmptyView();
            return;
        }
        setUploading(true);
        tvUploadStatus.setText(String.format(Locale.getDefault(),
                "正在上传并分割（%d/%d）…", index + 1, uris.size()));

        File videoFile = copyToCache(uris.get(index));
        if (videoFile == null) {
            ToastUtil.show(this, "视频读取失败，已跳过");
            uploadNext(uris, index + 1);
            return;
        }

        String mimeType = getContentResolver().getType(uris.get(index));
        RequestBody requestFile = RequestBody.create(
                MediaType.parse(mimeType != null ? mimeType : "video/mp4"), videoFile);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", videoFile.getName(), requestFile);

        apiService.uploadAiVideo(part).enqueue(new ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                videoFile.delete();
                Object url = data != null ? data.get("url") : null;
                if (url == null || url.toString().isEmpty()) {
                    ToastUtil.show(AiCreationEditActivity.this, "视频上传失败，已跳过");
                    uploadNext(uris, index + 1);
                    return;
                }
                splitVideo(url.toString(), uris, index);
            }

            @Override
            public void onError(String msg) {
                videoFile.delete();
                ToastUtil.show(AiCreationEditActivity.this, msg);
                uploadNext(uris, index + 1);
            }
        });
    }

    private void splitVideo(String videoUrl, List<Uri> uris, int index) {
        Map<String, Object> body = new HashMap<>();
        body.put("groupId", groupId);
        body.put("videoUrl", videoUrl);
        body.put("sliceDuration", sliceDuration);
        apiService.splitAiVideo(body).enqueue(new ApiCallback<List<BatchAiVideoClipDto>>() {
            @Override
            public void onSuccess(List<BatchAiVideoClipDto> clips) {
                if (clips != null) {
                    for (BatchAiVideoClipDto c : clips) {
                        String id = c.getClipId() != null ? String.valueOf(c.getClipId())
                                : String.valueOf(System.currentTimeMillis());
                        String text = c.getTextContent() != null ? c.getTextContent() : "";
                        double duration = c.getDuration() != null ? c.getDuration() : sliceDuration;
                        clipList.add(new VideoClip(id, c.getVideoUrl(), text, duration));
                    }
                    adapter.notifyDataSetChanged();
                    updateEmptyView();
                }
                uploadNext(uris, index + 1);
            }

            @Override
            public void onError(String msg) {
                ToastUtil.show(AiCreationEditActivity.this, "分割失败：" + msg);
                uploadNext(uris, index + 1);
            }
        });
    }

    private File copyToCache(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            String displayName = queryDisplayName(uri);
            if (displayName == null || displayName.isEmpty()) {
                displayName = "video_" + System.currentTimeMillis() + ".mp4";
            }
            File outFile = new File(getCacheDir(), "upload_" + System.currentTimeMillis() + "_" + displayName);
            try (InputStream in = resolver.openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(outFile)) {
                if (in == null) {
                    return null;
                }
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
            return outFile;
        } catch (Exception e) {
            return null;
        }
    }

    private String queryDisplayName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    return cursor.getString(nameIndex);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private void loadGroupDetail(Long groupId) {
        showLoading(true);
        apiService.getAiVideoGroup(groupId).enqueue(new ApiCallback<BatchAiVideoGroupDto>() {
            @Override
            public void onSuccess(BatchAiVideoGroupDto data) {
                showLoading(false);
                if (data == null) {
                    updateEmptyView();
                    return;
                }
                if (data.getGroupName() != null) {
                    etGroupName.setText(data.getGroupName());
                }
                clipList.clear();
                List<BatchAiVideoClipDto> clips = data.getClips();
                if (clips != null) {
                    for (BatchAiVideoClipDto c : clips) {
                        String id = c.getClipId() != null ? String.valueOf(c.getClipId())
                                : String.valueOf(System.currentTimeMillis());
                        String text = c.getTextContent() != null ? c.getTextContent() : "";
                        double duration = c.getDuration() != null ? c.getDuration() : 2.0;
                        clipList.add(new VideoClip(id, c.getVideoUrl(), text, duration));
                    }
                }
                adapter.notifyDataSetChanged();
                updateEmptyView();
            }

            @Override
            public void onError(String msg) {
                showLoading(false);
                ToastUtil.show(AiCreationEditActivity.this, msg);
                updateEmptyView();
            }
        });
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
        if (uploading) {
            ToastUtil.show(this, "素材上传中，请稍候");
            return;
        }
        if (clipList.isEmpty()) {
            ToastUtil.show(this, "请先选择视频并生成分镜头");
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
        Map<String, Object> body = new HashMap<>();
        body.put("groupId", groupId);
        body.put("count", generateCount);
        apiService.submitAiVideoGenerate(body).enqueue(new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object data) {
                showLoading(false);
                refreshCustomerInfo();
                ToastUtil.show(AiCreationEditActivity.this, "已提交生成任务");
                Intent intent = new Intent(AiCreationEditActivity.this, SplitProgressActivity.class);
                intent.putExtra("group_id", groupId);
                startActivity(intent);
            }

            @Override
            public void onError(String msg) {
                showLoading(false);
                new AlertDialog.Builder(AiCreationEditActivity.this)
                        .setMessage(msg != null ? msg : AppConfig.getPowerExhaustedMessage(AiCreationEditActivity.this))
                        .setPositiveButton(R.string.confirm, null)
                        .show();
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

    private void setUploading(boolean value) {
        uploading = value;
        tvUploadStatus.setVisibility(value ? View.VISIBLE : View.GONE);
        btnPickVideo.setEnabled(!value);
        btnGenerate.setEnabled(!value);
    }

    private void updateEmptyView() {
        boolean empty = clipList.isEmpty();
        tvEmptyClips.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
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
            // 先移除监听再 setText，避免触发回写，最后恢复监听
            holder.etText.removeTextChangedListener(holder.textWatcher);
            holder.etText.setTag(position);
            holder.etText.setText(clip.getText());
            holder.etText.addTextChangedListener(holder.textWatcher);
            holder.tvIndex.setText(String.format(Locale.getDefault(), "分镜 %d", position + 1));
            holder.tvDuration.setText(String.format(Locale.getDefault(), "时长：%.1fs", clip.getDuration()));
            holder.ivDelete.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos >= 0 && pos < clipList.size()) {
                    confirmDeleteClip(pos);
                }
            });
        }

        @Override
        public int getItemCount() {
            return clipList.size();
        }

        private void confirmDeleteClip(int position) {
            new AlertDialog.Builder(AiCreationEditActivity.this)
                    .setTitle(R.string.confirm_delete)
                    .setMessage("确定删除该分镜头吗？")
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        clipList.remove(position);
                        notifyDataSetChanged();
                        updateEmptyView();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvIndex;
            EditText etText;
            TextView tvDuration;
            ImageView ivDelete;
            TextWatcher textWatcher;

            VH(View itemView) {
                super(itemView);
                tvIndex = itemView.findViewById(R.id.tv_index);
                etText = itemView.findViewById(R.id.et_text);
                tvDuration = itemView.findViewById(R.id.tv_duration);
                ivDelete = itemView.findViewById(R.id.iv_delete);
                // 监听只注册一次，bind 时仅更新 tag 与文本
                textWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        Object tag = etText.getTag();
                        if (tag instanceof Integer) {
                            int pos = (Integer) tag;
                            if (pos >= 0 && pos < clipList.size()) {
                                clipList.get(pos).setText(s.toString());
                            }
                        }
                    }
                };
                etText.addTextChangedListener(textWatcher);
            }
        }
    }
}
