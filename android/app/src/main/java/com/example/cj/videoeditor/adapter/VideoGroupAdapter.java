package com.example.cj.videoeditor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.bean.VideoGroup;
import java.util.List;

public class VideoGroupAdapter extends RecyclerView.Adapter<VideoGroupAdapter.VH> {

    private final List<VideoGroup> data;
    private final OnGroupActionListener listener;

    public interface OnGroupActionListener {
        void onGroupClick(VideoGroup group);
        void onGroupDelete(VideoGroup group);
    }

    public VideoGroupAdapter(List<VideoGroup> data, OnGroupActionListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_group, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        VideoGroup item = data.get(position);
        holder.tvName.setText(item.getName());
        holder.tvDate.setText(item.getCreateDate());
        holder.tvGenerated.setText("已生成 " + item.getGeneratedCount() + "/" + item.getMaxCount() + " 条");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onGroupClick(item);
        });
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onGroupDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDate;
        TextView tvGenerated;
        ImageView ivDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvGenerated = itemView.findViewById(R.id.tv_generated);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
