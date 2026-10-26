package com.ht.robotvacuumapp.model.entity;

/** Immutable robot data, independent of Android views and resources. */
public final class RobotDevice {
    private final String id;
    private final String name;
    private final String room;
    private final String status;
    private final int batteryPercentage;
    private final boolean square;

    public RobotDevice(String id, String name) {
        this(id, name, null, null, -1, false);
    }

    public RobotDevice(String id, String name, String room, String status, int batteryPercentage, boolean square) {
        this.id = id;
        this.name = name;
        this.room = room;
        this.status = status;
        this.batteryPercentage = batteryPercentage;
        this.square = square;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getRoom() { return room; }
    public String getStatus() { return status; }
    public int getBatteryPercentage() { return batteryPercentage; }
    public boolean isSquare() { return square; }
}
