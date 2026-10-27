package com.ht.robotvacuumapp.viewmodel.robot;

public final class RobotControlUiState {
    public enum Status { WORKING, PAUSED, CHARGING }
    public final String robotName;
    public final int batteryPercent;
    public final boolean autoMode;
    public final Status status;
    public final String message;
    public final String robotId;
    public final boolean nameUpdated;
    public final boolean robotUnpaired;

    public RobotControlUiState(String robotId, String robotName, int batteryPercent, boolean autoMode,
                               Status status, String message, boolean nameUpdated, boolean robotUnpaired) {
        this.robotId = robotId;
        this.nameUpdated = nameUpdated;
        this.robotUnpaired = robotUnpaired;
        this.robotName = robotName;
        this.batteryPercent = batteryPercent;
        this.autoMode = autoMode;
        this.status = status;
        this.message = message;
    }
}
