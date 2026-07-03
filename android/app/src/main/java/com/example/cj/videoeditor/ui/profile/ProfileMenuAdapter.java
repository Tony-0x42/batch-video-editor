package com.example.cj.videoeditor.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.bean.ProfileMenu;

import java.util.List;

public class ProfileMenuAdapter extends RecyclerView.Adapter<ProfileMenuAdapter.ViewHolder> {

    private final List<ProfileMenu> list;
    private final OnMenuClickListener listener;

    public ProfileMenuAdapter(List<ProfileMenu> list, OnMenuClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProfileMenu item = list.get(position);
        holder.tvTitle.setText(item.title);
        holder.ivIcon.setImageResource(item.iconRes);
        holder.tvExtra.setText(item.extra);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMenuClick(item, position);
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void updateExtra(int position, String extra) {
        if (position >= 0 && position < list.size()) {
            list.get(position).extra = extra;
            notifyItemChanged(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvExtra;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvExtra = itemView.findViewById(R.id.tv_extra);
        }
    }

    public interface OnMenuClickListener {
        void onMenuClick(ProfileMenu menu, int position);
    }
}