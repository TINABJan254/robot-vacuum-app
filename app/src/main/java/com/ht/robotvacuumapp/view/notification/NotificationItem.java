package com.ht.robotvacuumapp.view.notification;

import com.ht.robotvacuumapp.api.dto.AlertResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Display values derived from a validated backend response; unread is never toggled locally. */
public final class NotificationItem {
    private final long id;
    private final String title, deviceName, time, date, readAt;

    public NotificationItem(AlertResponse alert) {
        id = alert.getId();
        title = alert.getTitle();
        deviceName = alert.getRobotName();
        readAt = alert.getReadAt();
        Date created = new Date(alert.getCreatedAtMillis());
        time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(created);
        date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(created);
    }
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDeviceName() { return deviceName; }
    public String getTime() { return time; }
    public String getDate() { return date; }
    public boolean isUnread() { return readAt == null; }
}
