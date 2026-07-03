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
import com.example.cj.videoeditor.AppConfig;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.activity.AiCreationEditActivity;
import com.example.cj.videoeditor.adapter.VideoGroupAdapter;
import com.example.cj.videoeditor.model.VideoGroup;
import com.example.cj.videoeditor.utils.MockData;
import com.example.cj.videoeditor.utils.SharedPrefUtil;
import com.example.cj.videoeditor.utils.ToastUtil;
import java.util.ArrayList;
import java.util.List;

public class AiCreationFragment extends Fragment {

    private EditText etSearch;
    private TextView tvPowerPercent, tvUsedPower, tvTotalPower;
    private ProgressBar powerProgress;
    private RecyclerView recyclerGroups;
    private LinearLayout emptyView;
    private Button btnAddGroup, btnEmptyAdd;
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

        loadPower();
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
    }

    private void loadPower() {
        int total = SharedPrefUtil.getInt(requireContext(), AppConfig.SP_KEY_COMPUTE_TOTAL, 1000);
        int used = SharedPrefUtil.getInt(requireContext(), AppConfig.SP_KEY_COMPUTE_USED, 356);
        double percent = total == 0 ? 0 : (used * 100.0 / total);
        tvPowerPercent.setText(String.format(getString(R.string.power_percent_format), percent));
        tvUsedPower.setText(getString(R.string.used_compute_power, used));
        tvTotalPower.setText(getString(R.string.total_compute_power, total));
        powerProgress.setMax(total);
        powerProgress.setProgress(used);

        boolean exhausted = used >= total;
        btnAddGroup.setEnabled(!exhausted);
        btnAddGroup.setBackgroundResource(exhausted ? R.drawable.bg_button_disabled : R.drawable.bg_button_blue);
    }

    private void loadGroups() {
        allGroups = MockData.getVideoGroups();
        displayGroups.clear();
        displayGroups.addAll(allGroups);
        recyclerGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VideoGroupAdapter(displayGroups);
        recyclerGroups.setAdapter(adapter);
        adapter.setOnItemActionListener(new VideoGroupAdapter.OnItemActionListener() {
            @Override
            public void onEdit(VideoGroup group) {
                Intent intent = new Intent(getContext(), AiCreationEditActivity.class);
                intent.putExtra("group_id", group.id);
                intent.putExtra("group_name", group.name);
                startActivity(intent);
            }

            @Override
            public void onDelete(VideoGroup group, int position) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.confirm_delete)
                        .setMessage("删除后将清空本组全部素材与生成记录，是否继续？")
                        .setPositiveButton(R.string.confirm, (dialog, which) -> {
                            displayGroups.remove(position);
                            allGroups.remove(group);
                            adapter.notifyDataSetChanged();
                            updateEmptyView();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
        updateEmptyView();
    }

    private void filter(String keyword) {
        displayGroups.clear();
        if (keyword.trim().isEmpty()) {
            displayGroups.addAll(allGroups);
        } else {
            for (VideoGroup g : allGroups) {
                if (g.name.contains(keyword)) {
                    displayGroups.add(g);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void updateEmptyView() {
        boolean empty = displayGroups.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerGroups.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void onAddGroup() {
        int total = SharedPrefUtil.getInt(requireContext(), AppConfig.SP_KEY_COMPUTE_TOTAL, 1000);
        int used = SharedPrefUtil.getInt(requireContext(), AppConfig.SP_KEY_COMPUTE_USED, 356);
        if (used >= total) {
            new AlertDialog.Builder(requireContext())
                    .setMessage(R.string.power_exhausted)
                    .setPositiveButton(R.string.confirm, null)
                    .show();
            return;
        }
        VideoGroup group = new VideoGroup("g" + System.currentTimeMillis(), "新建视频组", "2026-07-03", 0, 10);
        allGroups.add(0, group);
        displayGroups.add(0, group);
        adapter.notifyItemInserted(0);
        updateEmptyView();
        ToastUtil.show(getContext(), R.string.add_group);
    }
}
