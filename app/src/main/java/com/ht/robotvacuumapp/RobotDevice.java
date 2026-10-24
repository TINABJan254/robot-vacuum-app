package com.ht.robotvacuumapp;

public class RobotDevice {
    private String id;
    private String name;
    private String room;
    private String status;
    private int batteryPercentage;
    private boolean isActive;
    private int iconResId;
    private boolean isAddButton;

    public RobotDevice() {
    }

    // Constructor for regular Robot device card
    public RobotDevice(String id, String name, String room, String status, int batteryPercentage, boolean isActive, int iconResId) {
        this.id = id;
        this.name = name;
        this.room = room;
        this.status = status;
        this.batteryPercentage = batteryPercentage;
        this.isActive = isActive;
        this.iconResId = iconResId;
        this.isAddButton = false;
    }

    // Factory method for "+ Thêm Robot" item
    public static RobotDevice createAddButtonItem() {
        RobotDevice item = new RobotDevice();
        item.isAddButton = true;
        return item;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(int batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public boolean isAddButton() {
        return isAddButton;
    }

    public void setAddButton(boolean addButton) {
        isAddButton = addButton;
    }
}
