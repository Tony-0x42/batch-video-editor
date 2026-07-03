package com.example.cj.videoeditor.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.adapter.BrandAdapter;
import com.example.cj.videoeditor.bean.Brand;
import com.example.cj.videoeditor.mock.MockData;
import com.example.cj.videoeditor.utils.ToastUtil;
import java.util.List;

public class BrandActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_brand;
    }

    @Override
    protected void initViews() {
        setTitle(getString(R.string.brand));
        RecyclerView rvBrands = findViewById(R.id.rv_brands);
        TextView tvEmpty = findViewById(R.id.tv_empty);
        rvBrands.setLayoutManager(new GridLayoutManager(this, 2));
        List<Brand> brands = MockData.getMockBrands();
        if (brands.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvBrands.setVisibility(View.GONE);
        } else {
            rvBrands.setAdapter(new BrandAdapter(brands, brand -> {
                ToastUtil.show(this, brand.getName() + "\n" + brand.getDetail());
            }));
        }
    }
}
