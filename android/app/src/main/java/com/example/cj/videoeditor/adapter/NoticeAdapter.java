package com.example.cj.videoeditor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.network.dto.BatchAppNoticeDto;
import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.VH> {

    private final List<BatchAppNoticeDto> data;
    private final OnNoticeClickListener listener;

    public interface OnNoticeClickListener {
        void onNoticeClick(BatchAppNoticeDto notice);
    }

    public NoticeAdapter(List<BatchAppNoticeDto> data, OnNoticeClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        BatchAppNoticeDto item = data.get(position);
        holder.tvTitle.setText(item.getNoticeTitle() != null ? item.getNoticeTitle() : "");
        holder.tvTime.setText(item.getPublishTime() != null ? item.getPublishTime() : "");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNoticeClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvTime;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
