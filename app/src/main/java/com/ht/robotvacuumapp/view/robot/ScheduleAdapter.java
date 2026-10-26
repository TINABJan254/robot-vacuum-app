package com.ht.robotvacuumapp.view.robot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ht.robotvacuumapp.R;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    public interface OnScheduleToggleListener {
        void onToggle(ScheduleItem item, boolean isChecked, int position);
    }

    private final List<ScheduleItem> items;
    private final OnScheduleToggleListener toggleListener;

    public ScheduleAdapter(List<ScheduleItem> items, OnScheduleToggleListener toggleListener) {
        this.items = items;
        this.toggleListener = toggleListener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_card, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ScheduleItem item = items.get(position);
        holder.tvTimezone.setText(item.getTimezone());
        holder.tvTime.setText(item.getTime());
        holder.tvMode.setText(item.getMode());
        holder.tvRepeat.setText(item.getRepeatDays());

        holder.switchSchedule.setOnCheckedChangeListener(null);
        holder.switchSchedule.setChecked(item.isEnabled());
        holder.switchSchedule.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setEnabled(isChecked);
            if (toggleListener != null) {
                toggleListener.onToggle(item, isChecked, holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimezone;
        TextView tvTime;
        TextView tvMode;
        TextView tvRepeat;
        SwitchCompat switchSchedule;

        ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimezone = itemView.findViewById(R.id.tv_schedule_timezone);
            tvTime = itemView.findViewById(R.id.tv_schedule_time);
            tvMode = itemView.findViewById(R.id.tv_schedule_mode);
            tvRepeat = itemView.findViewById(R.id.tv_schedule_repeat);
            switchSchedule = itemView.findViewById(R.id.switch_schedule);
        }
    }
}
