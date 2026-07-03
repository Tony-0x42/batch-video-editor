package com.example.cj.videoeditor.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.bean.HomeEntry;

import java.util.List;

public class HomeEntryAdapter extends RecyclerView.Adapter<HomeEntryAdapter.ViewHolder> {

    private final List<HomeEntry> list;
    private final OnEntryClickListener listener;

    public HomeEntryAdapter(List<HomeEntry> list, OnEntryClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeEntry item = list.get(position);
        holder.tvName.setText(item.name);
        holder.ivIcon.setImageResource(item.iconRes);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEntryClick(item, position);
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }

    public interface OnEntryClickListener {
        void onEntryClick(HomeEntry entry, int position);
    }
}