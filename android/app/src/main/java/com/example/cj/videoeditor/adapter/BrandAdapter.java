package com.example.cj.videoeditor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.bean.Brand;
import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.VH> {

    private final List<Brand> data;
    private final OnBrandClickListener listener;

    public interface OnBrandClickListener {
        void onBrandClick(Brand brand);
    }

    public BrandAdapter(List<Brand> data, OnBrandClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_brand, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Brand item = data.get(position);
        holder.tvName.setText(item.getName());
        holder.tvIntro.setText(item.getIntro());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBrandClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvName;
        TextView tvIntro;

        VH(@NonNull View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.iv_logo);
            tvName = itemView.findViewById(R.id.tv_name);
            tvIntro = itemView.findViewById(R.id.tv_intro);
        }
    }
}
