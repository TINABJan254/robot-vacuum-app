package com.ht.robotvacuumapp.view.robot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ht.robotvacuumapp.R;

import java.util.List;

public class CleaningHistoryAdapter extends RecyclerView.Adapter<CleaningHistoryAdapter.HistoryViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CleaningHistoryItem item);
    }

    private final List<CleaningHistoryItem> items;
    private final OnItemClickListener listener;

    public CleaningHistoryAdapter(List<CleaningHistoryItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cleaning_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        CleaningHistoryItem item = items.get(position);
        holder.tvDateTime.setText(item.getDateTime());
        holder.tvDetails.setText(item.getFormattedDetails());

        if (item.getStatus() == CleaningHistoryItem.Status.CUSTOM_OR_WARNING) {
            holder.viewDot.setBackgroundResource(R.drawable.bg_dot_history_orange);
        } else {
            holder.viewDot.setBackgroundResource(R.drawable.bg_dot_history_green);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        final View viewDot;
        final TextView tvDateTime;
        final TextView tvDetails;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            viewDot = itemView.findViewById(R.id.view_status_dot);
            tvDateTime = itemView.findViewById(R.id.tv_history_date_time);
            tvDetails = itemView.findViewById(R.id.tv_history_details);
        }
    }
}
