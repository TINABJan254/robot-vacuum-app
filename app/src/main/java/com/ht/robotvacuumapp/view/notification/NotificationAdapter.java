package com.ht.robotvacuumapp.view.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.ht.robotvacuumapp.R;
import com.ht.robotvacuumapp.api.dto.AlertResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    public interface OnNotificationClickListener { void onNotificationClick(long id); }
    private final List<NotificationItem> items = new ArrayList<>();
    private List<AlertResponse> displayed = Collections.emptyList();
    private final OnNotificationClickListener listener;

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submitAlerts(List<AlertResponse> alerts) {
        if (displayed.equals(alerts)) return;
        displayed = alerts;
        items.clear();
        for (AlertResponse alert : alerts) items.add(new NotificationItem(alert));
        notifyDataSetChanged();
    }

    @Override public long getItemId(int position) { return items.get(position).getId(); }

    @NonNull @Override public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDevice.setText(item.getDeviceName());
        holder.tvTime.setText(item.getTime());
        holder.tvDate.setText(item.getDate());
        holder.viewUnreadDot.setVisibility(item.isUnread() ? View.VISIBLE : View.GONE);
        ViewCompat.setStateDescription(holder.itemView, holder.itemView.getContext().getString(
                item.isUnread() ? R.string.notification_unread : R.string.notification_read));
        holder.itemView.setOnClickListener(v -> {
            int current = holder.getBindingAdapterPosition();
            if (listener != null && current != RecyclerView.NO_POSITION && current < items.size()) {
                listener.onNotificationClick(items.get(current).getId());
            }
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        final View viewUnreadDot;
        final TextView tvTitle, tvDevice, tvTime, tvDate;
        NotificationViewHolder(@NonNull View view) {
            super(view);
            viewUnreadDot = view.findViewById(R.id.view_unread_dot);
            tvTitle = view.findViewById(R.id.tv_notification_title);
            tvDevice = view.findViewById(R.id.tv_notification_device);
            tvTime = view.findViewById(R.id.tv_notification_time);
            tvDate = view.findViewById(R.id.tv_notification_date);
        }
    }
}
