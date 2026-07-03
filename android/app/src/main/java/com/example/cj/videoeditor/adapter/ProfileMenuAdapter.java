package com.example.cj.videoeditor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cj.videoeditor.R;
import java.util.List;

public class ProfileMenuAdapter extends RecyclerView.Adapter<ProfileMenuAdapter.VH> {

    public static class MenuItem {
        public String title;
        public String extra;
        public int iconRes;
        public Runnable action;

        public MenuItem(String title, String extra, int iconRes, Runnable action) {
            this.title = title;
            this.extra = extra;
            this.iconRes = iconRes;
            this.action = action;
        }
    }

    private final List<MenuItem> data;

    public ProfileMenuAdapter(List<MenuItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_menu, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MenuItem item = data.get(position);
        holder.ivIcon.setImageResource(item.iconRes);
        holder.tvTitle.setText(item.title);
        holder.tvExtra.setText(item.extra);
        holder.itemView.setOnClickListener(v -> {
            if (item.action != null) item.action.run();
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvExtra;

        VH(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvExtra = itemView.findViewById(R.id.tv_extra);
        }
    }
}
