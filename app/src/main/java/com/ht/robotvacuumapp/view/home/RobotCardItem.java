package com.ht.robotvacuumapp.view.home;

import com.ht.robotvacuumapp.R;
import com.ht.robotvacuumapp.model.entity.RobotDevice;

public final class RobotCardItem {
    public final RobotDevice device;
    public final boolean selected;
    public final int iconResId;

    public RobotCardItem(RobotDevice device, boolean selected) {
        this.device = device;
        this.selected = selected;
        iconResId = R.drawable.ic_robot_card_circle_gray;
    }
}
