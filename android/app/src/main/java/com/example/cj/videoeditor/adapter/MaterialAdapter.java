package com.example.cj.videoeditor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.bean.Material;
import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.VH> {

    private final List<Material> data;

    public MaterialAdapter(List<Material> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_material, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Material item = data.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvType.setText(item.getType().name());
        holder.tvMeta.setText(item.getPublishTime() + "  浏览 " + item.getViewCount());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;
        TextView tvType;
        TextView tvMeta;

        VH(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvType = itemView.findViewById(R.id.tv_type);
            tvMeta = itemView.findViewById(R.id.tv_meta);
        }
    }
}
