package com.ht.robotvacuumapp.view.robot;

import com.ht.robotvacuumapp.api.dto.ScheduleResponse;
import java.util.List;

/** Confirmed server data plus the transient state of one visible row. */
public final class ScheduleItem {
    private final long id;
    private final String time;
    private final List<Integer> days;
    private final boolean enabled, busy;
    public ScheduleItem(ScheduleResponse response, Boolean pendingEnabled, boolean deleting) {
        id = response.getId();
        String value = response.getTime();
        time = value.length() == 8 && value.endsWith(":00") ? value.substring(0, 5) : value;
        days = response.getDaysOfWeek();
        enabled = pendingEnabled == null ? response.isEnabled() : pendingEnabled;
        busy = pendingEnabled != null || deleting;
    }
    public long getId() { return id; }
    public String getTime() { return time; }
    public List<Integer> getDays() { return days; }
    public boolean isEnabled() { return enabled; }
    public boolean isBusy() { return busy; }
}
