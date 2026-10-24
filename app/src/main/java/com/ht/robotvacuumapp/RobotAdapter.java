package com.ht.robotvacuumapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RobotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DEVICE = 0;
    private static final int TYPE_ADD_BUTTON = 1;

    private final List<RobotDevice> robotList;
    private final OnRobotClickListener listener;

    public interface OnRobotClickListener {
        void onRobotClick(RobotDevice robot, int position);
        void onAddRobotClick();
    }

    public RobotAdapter(List<RobotDevice> robotList, OnRobotClickListener listener) {
        this.robotList = robotList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (robotList.get(position).isAddButton()) {
            return TYPE_ADD_BUTTON;
        }
        return TYPE_DEVICE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ADD_BUTTON) {
            View view = inflater.inflate(R.layout.item_robot_add, parent, false);
            return new AddButtonViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_robot_device, parent, false);
            return new DeviceViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RobotDevice device = robotList.get(position);
        if (holder instanceof DeviceViewHolder) {
            ((DeviceViewHolder) holder).bind(device, position, listener);
        } else if (holder instanceof AddButtonViewHolder) {
            ((AddButtonViewHolder) holder).bind(listener);
        }
    }

    @Override
    public int getItemCount() {
        return robotList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final View cardContainer;
        private final TextView tvStatusBadge;
        private final TextView tvBattery;
        private final ImageView imgRobot;
        private final TextView tvRobotName;
        private final TextView tvRobotRoom;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_container);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvBattery = itemView.findViewById(R.id.tv_battery);
            imgRobot = itemView.findViewById(R.id.img_robot);
            tvRobotName = itemView.findViewById(R.id.tv_robot_name);
            tvRobotRoom = itemView.findViewById(R.id.tv_robot_room);
        }

        public void bind(RobotDevice device, int position, OnRobotClickListener listener) {
            tvRobotName.setText(device.getName());
            tvRobotRoom.setText(device.getRoom());
            tvBattery.setText(device.getBatteryPercentage() + "%");
            imgRobot.setImageResource(device.getIconResId());

            // Active border
            if (device.isActive()) {
                cardContainer.setBackgroundResource(R.drawable.bg_card_robot_active);
            } else {
                cardContainer.setBackgroundResource(R.drawable.bg_card_robot_normal);
            }

            // Status styling
            String status = device.getStatus();
            if ("Sẵn sàng".equalsIgnoreCase(status)) {
                tvStatusBadge.setText("● " + status);
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_status_ready);
                tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.primary_green));
            } else {
                tvStatusBadge.setText(status);
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_status_normal);
                tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            }

            cardContainer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRobotClick(device, position);
                }
            });
        }
    }

    static class AddButtonViewHolder extends RecyclerView.ViewHolder {
        private final View cardAddContainer;

        public AddButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            cardAddContainer = itemView.findViewById(R.id.card_add_container);
        }

        public void bind(OnRobotClickListener listener) {
            cardAddContainer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddRobotClick();
                }
            });
        }
    }
}
