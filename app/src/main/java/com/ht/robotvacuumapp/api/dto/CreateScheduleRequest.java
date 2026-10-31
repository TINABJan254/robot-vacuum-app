package com.ht.robotvacuumapp.api.dto;

import com.google.gson.annotations.SerializedName;
import java.util.*;

public final class CreateScheduleRequest {
    @SerializedName("robotId") private final long robotId;
    @SerializedName("time") private final String time;
    @SerializedName("enabled") private final boolean enabled;
    @SerializedName("daysOfWeek") private final List<Integer> daysOfWeek;

    public CreateScheduleRequest(long robotId, String time, boolean enabled, List<Integer> daysOfWeek) {
        if (robotId <= 0 || daysOfWeek == null) throw new IllegalArgumentException("Robot and days are required");
        Set<Integer> days = new TreeSet<>();
        for (Integer day : daysOfWeek) {
            if (day == null || day < 1 || day > 7 || !days.add(day)) throw new IllegalArgumentException("Invalid repeat days");
        }
        this.robotId = robotId;
        this.time = ScheduleResponse.normalizeTime(time);
        this.enabled = enabled;
        this.daysOfWeek = Collections.unmodifiableList(new ArrayList<>(days));
    }
    public long getRobotId() { return robotId; }
    public String getTime() { return time; }
    public boolean isEnabled() { return enabled; }
    public List<Integer> getDaysOfWeek() { return daysOfWeek; }
}
