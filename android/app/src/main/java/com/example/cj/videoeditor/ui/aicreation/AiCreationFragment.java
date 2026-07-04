package com.example.cj.videoeditor.ui.aicreation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.activity.AiCreationEditActivity;
import com.example.cj.videoeditor.adapter.VideoGroupAdapter;
import com.example.cj.videoeditor.bean.VideoGroup;
import com.example.cj.videoeditor.network.ApiCallback;
import com.example.cj.videoeditor.network.ApiService;
import com.example.cj.videoeditor.network.PageApiCallback;
import com.example.cj.videoeditor.network.dto.BatchAiVideoGroupCreateResultDto;
import com.example.cj.videoeditor.network.dto.BatchAiVideoGroupDto;
import com.example.cj.videoeditor.network.dto.BatchCustomerDto;
import com.example.cj.videoeditor.utils.AppConfig;
import com.example.cj.videoeditor.utils.SharedPrefUtil;
import com.example.cj.videoeditor.utils.ToastUtil;
import com.example.cj.videoeditor.utils.UserStore;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AiCreationFragment extends Fragment {

    @Inject
    ApiService apiService;

    private EditText etSearch;
    private TextView tvPowerPercent, tvUsedPower, tvTotalPower;
    private ProgressBar powerProgress;
    private RecyclerView recyclerGroups;
    private LinearLayout emptyView;
    private Button btnAddGroup, btnEmptyAdd;
    private ProgressBar loadingProgress;
    private VideoGroupAdapter adapter;
    private List<VideoGroup> allGroups = new ArrayList<>();
    private List<VideoGroup> displayGroups = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_creation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etSearch = view.findViewById(R.id.et_search);
        tvPowerPercent = view.findViewById(R.id.tv_power_percent);
        tvUsedPower = view.findViewById(R.id.tv_used_power);
        tvTotalPower = view.findViewById(R.id.tv_total_power);
        powerProgress = view.findViewById(R.id.power_progress);
        recyclerGroups = view.findViewById(R.id.recycler_groups);
        emptyView = view.findViewById(R.id.empty_view);
        btnAddGroup = view.findViewById(R.id.btn_add_group);
        btnEmptyAdd = view.findViewById(R.id.btn_empty_add);
        loadingProgress = view.findViewById(R.id.loading_progress);

        loadPower();
        setupRecyclerView();
        loadGroups();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        btnAddGroup.setOnClickListener(v -> onAddGroup());
        btnEmptyAdd.setOnClickListener(v -> onAddGroup());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPower();
        refreshCustomerInfo();
        loadGroups();
    }

    private void setupRecyclerView() {
        recyclerGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VideoGroupAdapter(displayGroups);
        recyclerGroups.setAdapter(adapter);
        adapter.setOnGroupActionListener(new VideoGroupAdapter.OnGroupActionListener() {
            @Override
            public void onGroupClick(VideoGroup group) {
                Intent intent = new Intent(getContext(), AiCreationEditActivity.class);
                try {
                    intent.putExtra("group_id", Long.parseLong(group.id));
                } catch (NumberFormatException e) {
                    intent.putExtra("group_id", -1L);
                }
                intent.putExtra("group_name", group.name);
                startActivity(intent);
            }

            @Override
            public void onGroupDelete(VideoGroup group) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.confirm_delete)
                        .setMessage("删除后将清空本组全部素材与生成记录，是否继续？")
                        .setPositiveButton(R.string.confirm, (dialog, which) -> deleteGroup(group))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
    }

    private void loadPower() {
        int total = SharedPrefUtil.getInt(requireContext(), AppConfig.SP_KEY_COMPUTE_TOTAL, 0);
        int used = SharedPrefUtil.getInt(requireContext(), AppConfig.SP_KEY_COMPUTE_USED, 0);
        double percent = total == 0 ? 0 : (used * 100.0 / total);
        tvPowerPercent.setText(String.format(getString(R.string.power_percent_format), percent));
        tvUsedPower.setText(getString(R.string.used_compute_power, used));
        tvTotalPower.setText(getString(R.string.total_compute_power, total));
        powerProgress.setMax(Math.max(total, 1));
        powerProgress.setProgress(used);

        boolean exhausted = used >= total;
        btnAddGroup.setEnabled(!exhausted);
        btnAddGroup.setBackgroundResource(exhausted ? R.drawable.bg_button_disabled : R.drawable.bg_button_blue);
    }

    private void refreshCustomerInfo() {
        String phone = SharedPrefUtil.getString(requireContext(), AppConfig.SP_KEY_USER_PHONE, "");
        if (phone.isEmpty()) {
            return;
        }
        apiService.getAppCustomer(phone).enqueue(new ApiCallback<BatchCustomerDto>() {
            @Override
            public void onSuccess(BatchCustomerDto data) {
                if (data != null) {
                    UserStore.saveCustomerDto(requireContext(), data);
                    loadPower();
                }
            }

            @Override
            public void onError(String msg) {
                // 静默失败，下次进入再刷新
            }
        });
    }

    private void loadGroups() {
        showLoading(true);
        apiService.getAiVideoGroupList().enqueue(new PageApiCallback<BatchAiVideoGroupDto>() {
            @Override
            public void onSuccess(long total, List<BatchAiVideoGroupDto> rows) {
                showLoading(false);
                allGroups.clear();
                for (BatchAiVideoGroupDto dto : rows) {
                    allGroups.add(mapToVideoGroup(dto));
                }
                filter(etSearch.getText().toString());
            }

            @Override
            public void onError(String msg) {
                showLoading(false);
                ToastUtil.show(requireContext(), msg);
                updateEmptyView();
            }
        });
    }

    private VideoGroup mapToVideoGroup(BatchAiVideoGroupDto dto) {
        String date = dto.getCreateTime();
        if (date != null && date.length() >= 10) {
            date = date.substring(0, 10);
        } else {
            date = "";
        }
        Long groupId = dto.getGroupId() != null ? dto.getGroupId() : 0L;
        String name = dto.getGroupName() != null ? dto.getGroupName() : "";
        int generated = dto.getGeneratedCount() != null ? dto.getGeneratedCount() : 0;
        int maxLimit = dto.getMaxLimit() != null ? dto.getMaxLimit() : AppConfig.getMaxVideos(requireContext());
        return new VideoGroup(String.valueOf(groupId), name, date, generated, maxLimit);
    }

    private void deleteGroup(VideoGroup group) {
        try {
            long groupId = Long.parseLong(group.id);
            showLoading(true);
            apiService.deleteAiVideoGroup(groupId).enqueue(new ApiCallback<Object>() {
                @Override
                public void onSuccess(Object data) {
                    showLoading(false);
                    int position = displayGroups.indexOf(group);
                    displayGroups.remove(group);
                    allGroups.remove(group);
                    if (position >= 0) {
                        adapter.notifyItemRemoved(position);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    updateEmptyView();
                }

                @Override
                public void onError(String msg) {
                    showLoading(false);
                    ToastUtil.show(requireContext(), msg);
                }
            });
        } catch (NumberFormatException e) {
            ToastUtil.show(requireContext(), "视频组 ID 异常");
        }
    }

    private void filter(String keyword) {
        displayGroups.clear();
        if (keyword == null || keyword.trim().isEmpty()) {
            displayGroups.addAll(allGroups);
        } else {
            for (VideoGroup g : allGroups) {
                if (g.name != null && g.name.contains(keyword.trim())) {
                    displayGroups.add(g);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateEmptyView();
    }

    private void updateEmptyView() {
        boolean empty = displayGroups.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerGroups.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void onAddGroup() {
        int total = SharedPrefUtil.getInt(requireContext(), AppConfig.SP_KEY_COMPUTE_TOTAL, 0);
        int used = SharedPrefUtil.getInt(requireContext(), AppConfig.SP_KEY_COMPUTE_USED, 0);
        if (used >= total) {
            new AlertDialog.Builder(requireContext())
                    .setMessage(AppConfig.getPowerExhaustedMessage(requireContext()))
                    .setPositiveButton(R.string.confirm, null)
                    .show();
            return;
        }

        BatchAiVideoGroupDto dto = new BatchAiVideoGroupDto();
        dto.setGroupName("新建视频组");
        showLoading(true);
        apiService.addAiVideoGroup(dto).enqueue(new ApiCallback<BatchAiVideoGroupCreateResultDto>() {
            @Override
            public void onSuccess(BatchAiVideoGroupCreateResultDto data) {
                showLoading(false);
                Long groupId = data != null && data.getGroupId() != null ? data.getGroupId() : 0L;
                String date = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(new java.util.Date());
                VideoGroup group = new VideoGroup(String.valueOf(groupId), dto.getGroupName(), date, 0, AppConfig.getMaxVideos(requireContext()));
                allGroups.add(0, group);
                filter(etSearch.getText().toString());
                ToastUtil.show(getContext(), R.string.add_group);
            }

            @Override
            public void onError(String msg) {
                showLoading(false);
                ToastUtil.show(requireContext(), msg);
            }
        });
    }

    private void showLoading(boolean show) {
        if (loadingProgress != null) {
            loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
