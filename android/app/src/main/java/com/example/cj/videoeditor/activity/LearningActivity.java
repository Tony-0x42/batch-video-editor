package com.example.cj.videoeditor.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.adapter.MaterialAdapter;
import com.example.cj.videoeditor.bean.Material;
import com.example.cj.videoeditor.mock.MockData;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class LearningActivity extends BaseActivity {

    private TabLayout tabLayout;
    private RecyclerView rvMaterial;
    private TextView tvEmpty;
    private List<Material> allMaterials;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_learning;
    }

    @Override
    protected void initViews() {
        setTitle(getString(R.string.learning));
        tabLayout = findViewById(R.id.tab_layout);
        rvMaterial = findViewById(R.id.rv_material);
        tvEmpty = findViewById(R.id.tv_empty);
        rvMaterial.setLayoutManager(new LinearLayoutManager(this));

        tabLayout.addTab(tabLayout.newTab().setText("全部"));
        tabLayout.addTab(tabLayout.newTab().setText("视频"));
        tabLayout.addTab(tabLayout.newTab().setText("文档"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterMaterials(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    protected void initData() {
        allMaterials = MockData.getMockMaterials();
        filterMaterials(0);
    }

    private void filterMaterials(int position) {
        List<Material> filtered = new ArrayList<>();
        if (position == 0) {
            filtered.addAll(allMaterials);
        } else if (position == 1) {
            for (Material m : allMaterials) {
                if (m.getType() == Material.Type.VIDEO) filtered.add(m);
            }
        } else {
            for (Material m : allMaterials) {
                if (m.getType() != Material.Type.VIDEO) filtered.add(m);
            }
        }
        rvMaterial.setAdapter(new MaterialAdapter(filtered));
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        rvMaterial.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
