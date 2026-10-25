package com.ht.robotvacuumapp.viewmodel.home;

import com.ht.robotvacuumapp.model.entity.RobotDevice;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MainUiState {
    public final List<RobotDevice> robots;
    public final String selectedRobotId;
    public final int selectedTab;
    public final boolean requiresLogin;
    public final String robotIdError;
    public final String robotCodeError;
    public final boolean robotAdded;
    public final RobotDevice robotToOpen;
    public final String message;

    public MainUiState(List<RobotDevice> robots, String selectedRobotId, int selectedTab, boolean requiresLogin,
                       String robotIdError, String robotCodeError, boolean robotAdded,
                       RobotDevice robotToOpen, String message) {
        this.robots = Collections.unmodifiableList(new ArrayList<>(robots));
        this.selectedRobotId = selectedRobotId;
        this.selectedTab = selectedTab;
        this.requiresLogin = requiresLogin;
        this.robotIdError = robotIdError;
        this.robotCodeError = robotCodeError;
        this.robotAdded = robotAdded;
        this.robotToOpen = robotToOpen;
        this.message = message;
    }
}
