package com.example.cj.videoeditor.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.adapter.DocumentAdapter;
import com.example.cj.videoeditor.bean.Document;
import com.example.cj.videoeditor.mock.MockData;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class DocumentActivity extends BaseActivity {

    private RecyclerView rvDocuments;
    private TextView tvEmpty;
    private List<Document> allDocuments;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_document;
    }

    @Override
    protected void initViews() {
        setTitle("新手指南");
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        rvDocuments = findViewById(R.id.rv_documents);
        tvEmpty = findViewById(R.id.tv_empty);
        rvDocuments.setLayoutManager(new LinearLayoutManager(this));

        tabLayout.addTab(tabLayout.newTab().setText("全部"));
        tabLayout.addTab(tabLayout.newTab().setText("快速上手"));
        tabLayout.addTab(tabLayout.newTab().setText("操作指南"));
        tabLayout.addTab(tabLayout.newTab().setText("常见问题"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterDocuments(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    protected void initData() {
        allDocuments = MockData.getMockDocuments();
        filterDocuments(0);
    }

    private void filterDocuments(int position) {
        List<Document> filtered = new ArrayList<>();
        if (position == 0) {
            filtered.addAll(allDocuments);
        } else {
            String[] categories = {"", "快速上手", "操作指南", "常见问题"};
            String target = categories[position];
            for (Document d : allDocuments) {
                if (target.equals(d.getCategory())) filtered.add(d);
            }
        }
        rvDocuments.setAdapter(new DocumentAdapter(filtered, document -> {
            new AlertDialog.Builder(this)
                    .setTitle(document.getTitle())
                    .setMessage(document.getContent())
                    .setPositiveButton(R.string.confirm, null)
                    .show();
        }));
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        rvDocuments.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
