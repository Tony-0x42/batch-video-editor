package com.example.cj.videoeditor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.bean.Clip;
import java.util.List;

public class ClipAdapter extends RecyclerView.Adapter<ClipAdapter.VH> {

    private final List<Clip> data;
    private final OnClipActionListener listener;

    public interface OnClipActionListener {
        void onClipClick(Clip clip);
        void onClipDelete(Clip clip);
    }

    public ClipAdapter(List<Clip> data, OnClipActionListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clip, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Clip item = data.get(position);
        holder.tvIndex.setText("镜头 " + item.getIndex());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClipClick(item);
        });
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onClipDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivDelete;
        TextView tvIndex;

        VH(@NonNull View itemView) {
            super(itemView);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            tvIndex = itemView.findViewById(R.id.tv_index);
        }
    }
}
