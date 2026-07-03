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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.bean.User;
import com.example.cj.videoeditor.bean.VideoClip;
import com.example.cj.videoeditor.utils.MockDataProvider;
import com.example.cj.videoeditor.utils.ToastUtil;
import com.example.cj.videoeditor.utils.UserStore;

import java.util.ArrayList;
import java.util.List;

public class AiCreationEditActivity extends BaseActivity {

    private EditText etGroupName;
    private RecyclerView recyclerView;
    private Button btnAddClip;
    private Button btnGenerate;
    private ClipAdapter adapter;
    private List<VideoClip> clipList = new ArrayList<>();

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_ai_creation_edit;
    }

    @Override
    protected void initViews() {
        setTitle(getString(R.string.edit_group));
        String groupName = getIntent().getStringExtra("group_name");

        etGroupName = findViewById(R.id.et_group_name);
        recyclerView = findViewById(R.id.recycler_view);
        btnAddClip = findViewById(R.id.btn_add_clip);
        btnGenerate = findViewById(R.id.btn_generate);

        etGroupName.setText(groupName == null ? "" : groupName);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClipAdapter();
        recyclerView.setAdapter(adapter);

        clipList.add(new VideoClip(String.valueOf(System.currentTimeMillis()), "", "分镜头 1", 2.0));
        adapter.notifyDataSetChanged();

        btnAddClip.setOnClickListener(v -> {
            if (clipList.size() >= 10) {
                ToastUtil.show(this, "最多 10 个分镜头");
                return;
            }
            clipList.add(new VideoClip(String.valueOf(System.currentTimeMillis()), "", "分镜头 " + (clipList.size() + 1), 2.0));
            adapter.notifyDataSetChanged();
        });

        btnGenerate.setOnClickListener(v -> attemptGenerate());
    }

    private void attemptGenerate() {
        User user = UserStore.getUser(this);
        if (user == null) {
            user = MockDataProvider.getMockUser();
        }
        if (user.getUsedComputePower() >= user.getComputePower()) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.power_exhausted)
                    .setPositiveButton(R.string.confirm, null)
                    .show();
            return;
        }
        ToastUtil.show(this, "开始 AI 生成");
        startActivity(new Intent(this, SplitProgressActivity.class));
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
            holder.tvDuration.setText(String.format(java.util.Locale.getDefault(), "时长：%.1fs", clip.getDuration()));
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
