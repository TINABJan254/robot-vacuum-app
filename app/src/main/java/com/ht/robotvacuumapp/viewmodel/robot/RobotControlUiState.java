package com.ht.robotvacuumapp.viewmodel.robot;

import com.ht.robotvacuumapp.model.entity.RobotTelemetry;

/** Immutable screen state; telemetry is independent of confirmed name/unpair results. */
public final class RobotControlUiState {
    public final String robotId;
    public final String robotName;
    public final RobotTelemetry telemetry;
    public final boolean isLoading;
    public final boolean telemetryLoaded;
    public final String telemetryError;
    public final boolean requiresLogin;
    public final String message;
    public final boolean nameUpdated;
    public final boolean robotUnpaired;

    private RobotControlUiState(Builder b) {
        robotId = b.robotId;
        robotName = b.robotName;
        telemetry = b.telemetry;
        isLoading = b.isLoading;
        telemetryLoaded = b.telemetryLoaded;
        telemetryError = b.telemetryError;
        requiresLogin = b.requiresLogin;
        message = b.message;
        nameUpdated = b.nameUpdated;
        robotUnpaired = b.robotUnpaired;
    }

    Builder toBuilder() { return new Builder(this); }

    static final class Builder {
        String robotId;
        String robotName;
        RobotTelemetry telemetry;
        boolean isLoading;
        boolean telemetryLoaded;
        String telemetryError;
        boolean requiresLogin;
        String message;
        boolean nameUpdated;
        boolean robotUnpaired;

        Builder() { }
        Builder(RobotControlUiState s) {
            robotId = s.robotId;
            robotName = s.robotName;
            telemetry = s.telemetry;
            isLoading = s.isLoading;
            telemetryLoaded = s.telemetryLoaded;
            telemetryError = s.telemetryError;
            requiresLogin = s.requiresLogin;
            message = s.message;
            nameUpdated = s.nameUpdated;
            robotUnpaired = s.robotUnpaired;
        }
        RobotControlUiState build() { return new RobotControlUiState(this); }
    }
}
