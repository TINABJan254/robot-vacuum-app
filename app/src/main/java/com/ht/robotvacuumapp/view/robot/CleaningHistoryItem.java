package com.ht.robotvacuumapp.view.robot;

public class CleaningHistoryItem {
    public enum Status {
        SUCCESS,
        CUSTOM_OR_WARNING
    }

    private final String dateTime;
    private final String mode;
    private final String duration;
    private final Status status;

    public CleaningHistoryItem(String dateTime, String mode, String duration, Status status) {
        this.dateTime = dateTime;
        this.mode = mode;
        this.duration = duration;
        this.status = status;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getMode() {
        return mode;
    }

    public String getDuration() {
        return duration;
    }

    public Status getStatus() {
        return status;
    }

    public String getFormattedDetails() {
        return mode + " | " + duration;
    }
}
