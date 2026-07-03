package com.example.cj.videoeditor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.bean.Document;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.VH> {

    private final List<Document> data;
    private final OnDocumentClickListener listener;

    public interface OnDocumentClickListener {
        void onDocumentClick(Document document);
    }

    public DocumentAdapter(List<Document> data, OnDocumentClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Document item = data.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvUpdateTime.setText(item.getUpdateTime());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDocumentClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvUpdateTime;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvUpdateTime = itemView.findViewById(R.id.tv_update_time);
        }
    }
}
