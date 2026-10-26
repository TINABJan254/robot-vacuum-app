package com.ht.robotvacuumapp.view.robot;

public class ScheduleItem {
    private String id;
    private String time;
    private String timezone;
    private String mode;
    private String repeatDays;
    private boolean enabled;

    public ScheduleItem(String id, String time, String timezone, String mode, String repeatDays, boolean enabled) {
        this.id = id;
        this.time = time;
        this.timezone = timezone;
        this.mode = mode;
        this.repeatDays = repeatDays;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(String repeatDays) {
        this.repeatDays = repeatDays;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
