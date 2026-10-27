package com.ht.robotvacuumapp.model.entity;

/** One backend snapshot; a null mode means the robot has not reported its mode. */
public final class RobotTelemetry {
    public enum Status { IDLE, WORKING, RETURNING_TO_DOCK, CHARGING, ERROR }
    public enum Mode { AUTO, MANUAL }

    public final long robotId;
    public final int batteryPercent;
    public final Status status;
    public final Mode mode;
    public final String sentAt;
    public final String receivedAt;
    public final boolean stale;

    public RobotTelemetry(long robotId, int batteryPercent, Status status, Mode mode,
                          String sentAt, String receivedAt, boolean stale) {
        this.robotId = robotId;
        this.batteryPercent = batteryPercent;
        this.status = status;
        this.mode = mode;
        this.sentAt = sentAt;
        this.receivedAt = receivedAt;
        this.stale = stale;
    }
}
