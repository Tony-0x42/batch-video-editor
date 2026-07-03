package com.example.cj.videoeditor.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.bean.ContactPerson;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private final List<ContactPerson> list;
    private final OnCallListener listener;

    public ContactAdapter(List<ContactPerson> list, OnCallListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_person, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactPerson item = list.get(position);
        holder.tvName.setText(item.name);
        holder.tvRegion.setText(item.region);
        holder.tvPhone.setText(item.phone);
        holder.tvPhone.setOnClickListener(v -> {
            if (listener != null) listener.onCall(item.phone);
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRegion, tvPhone;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRegion = itemView.findViewById(R.id.tv_region);
            tvPhone = itemView.findViewById(R.id.tv_phone);
        }
    }

    public interface OnCallListener {
        void onCall(String phone);
    }
}