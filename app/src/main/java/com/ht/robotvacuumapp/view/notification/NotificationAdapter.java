package com.ht.robotvacuumapp.view.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ht.robotvacuumapp.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem item, int position);
    }

    private final List<NotificationItem> items;
    private final OnNotificationClickListener listener;

    public NotificationAdapter(List<NotificationItem> items, OnNotificationClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDevice.setText(item.getDeviceName());
        holder.tvTime.setText(item.getTime());
        holder.tvDate.setText(item.getDate());
        holder.viewUnreadDot.setVisibility(item.isUnread() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(item, holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        final View viewUnreadDot;
        final TextView tvTitle;
        final TextView tvDevice;
        final TextView tvTime;
        final TextView tvDate;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            viewUnreadDot = itemView.findViewById(R.id.view_unread_dot);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvDevice = itemView.findViewById(R.id.tv_notification_device);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            tvDate = itemView.findViewById(R.id.tv_notification_date);
        }
    }
}
