package com.ht.robotvacuumapp.view.notification;

public class NotificationItem {
    private final String title;
    private final String deviceName;
    private final String time;
    private final String date;
    private boolean unread;

    public NotificationItem(String title, String deviceName, String time, String date, boolean unread) {
        this.title = title;
        this.deviceName = deviceName;
        this.time = time;
        this.date = date;
        this.unread = unread;
    }

    public String getTitle() {
        return title;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }
}
