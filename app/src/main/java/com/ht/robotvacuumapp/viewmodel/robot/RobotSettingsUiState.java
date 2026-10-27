package com.ht.robotvacuumapp.viewmodel.robot;

import com.ht.robotvacuumapp.model.entity.RobotDevice;

public final class RobotSettingsUiState {
    public final String robotId;
    public final String robotName;
    public final String draftName;
    public final boolean dialogOpen;
    public final boolean isSaving;
    public final String nameError;
    public final String requestError;
    // Kept after acknowledging the Toast so Activity results survive recreation.
    public final RobotDevice updatedRobot;
    public final boolean requiresLogin;
    public final String message;
    public final boolean unpairDialogOpen;
    public final boolean isUnpairing;
    public final String unpairError;
    public final String unpairedRobotId;

    private RobotSettingsUiState(Builder b) {
        robotId = b.robotId;
        robotName = b.robotName;
        draftName = b.draftName;
        dialogOpen = b.dialogOpen;
        isSaving = b.isSaving;
        nameError = b.nameError;
        requestError = b.requestError;
        updatedRobot = b.updatedRobot;
        requiresLogin = b.requiresLogin;
        message = b.message;
        unpairDialogOpen = b.unpairDialogOpen;
        isUnpairing = b.isUnpairing;
        unpairError = b.unpairError;
        unpairedRobotId = b.unpairedRobotId;
    }

    Builder toBuilder() { return new Builder(this); }

    static final class Builder {
        String robotId;
        String robotName = "";
        String draftName = "";
        boolean dialogOpen;
        boolean isSaving;
        String nameError;
        String requestError;
        RobotDevice updatedRobot;
        boolean requiresLogin;
        String message;
        boolean unpairDialogOpen;
        boolean isUnpairing;
        String unpairError;
        String unpairedRobotId;

        Builder() { }
        Builder(RobotSettingsUiState s) {
            robotId = s.robotId;
            robotName = s.robotName;
            draftName = s.draftName;
            dialogOpen = s.dialogOpen;
            isSaving = s.isSaving;
            nameError = s.nameError;
            requestError = s.requestError;
            updatedRobot = s.updatedRobot;
            requiresLogin = s.requiresLogin;
            message = s.message;
            unpairDialogOpen = s.unpairDialogOpen;
            isUnpairing = s.isUnpairing;
            unpairError = s.unpairError;
            unpairedRobotId = s.unpairedRobotId;
        }
        RobotSettingsUiState build() { return new RobotSettingsUiState(this); }
    }
}
