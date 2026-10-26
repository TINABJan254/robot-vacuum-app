package com.ht.robotvacuumapp.view.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ht.robotvacuumapp.R;
import com.ht.robotvacuumapp.model.entity.RobotDevice;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RobotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DEVICE = 0;
    private static final int TYPE_ADD_BUTTON = 1;

    private final List<RobotCardItem> robotList = new ArrayList<>();
    private final OnRobotClickListener listener;
    private List<RobotDevice> displayedRobots = new ArrayList<>();
    private String selectedRobotId;

    public interface OnRobotClickListener {
        void onRobotClick(RobotDevice robot);
        void onAddRobotClick();
    }

    public RobotAdapter(OnRobotClickListener listener) {
        this.listener = listener;
    }

    public void submitRobots(List<RobotDevice> robots, String selectedId) {
        if (displayedRobots.equals(robots) && Objects.equals(selectedRobotId, selectedId)) return;
        displayedRobots = new ArrayList<>(robots);
        selectedRobotId = selectedId;
        robotList.clear();
        for (RobotDevice robot : robots) {
            robotList.add(new RobotCardItem(robot, Objects.equals(robot.getId(), selectedId)));
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position == robotList.size() ? TYPE_ADD_BUTTON : TYPE_DEVICE;
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
        if (holder instanceof DeviceViewHolder) {
            ((DeviceViewHolder) holder).bind(robotList.get(position), listener);
        } else if (holder instanceof AddButtonViewHolder) {
            ((AddButtonViewHolder) holder).bind(listener);
        }
    }

    @Override
    public int getItemCount() {
        return robotList.isEmpty() ? 0 : robotList.size() + 1;
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

        public void bind(RobotCardItem item, OnRobotClickListener listener) {
            RobotDevice device = item.device;
            tvRobotName.setText(device.getName());
            tvRobotRoom.setText(itemView.getContext().getString(R.string.robot_id_label, device.getId()));
            tvBattery.setVisibility(View.INVISIBLE);
            tvStatusBadge.setVisibility(View.INVISIBLE);
            imgRobot.setImageResource(item.iconResId);

            // Active border
            if (item.selected) {
                cardContainer.setBackgroundResource(R.drawable.bg_card_robot_active);
            } else {
                cardContainer.setBackgroundResource(R.drawable.bg_card_robot_normal);
            }

            cardContainer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRobotClick(device);
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
