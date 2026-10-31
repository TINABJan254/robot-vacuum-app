package com.ht.robotvacuumapp.api.dto;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.ht.robotvacuumapp.api.dto.JsonResponseValues.*;

public final class ScheduleResponse {
    private static final Pattern TIME = Pattern.compile("([01][0-9]|2[0-3]):([0-5][0-9])(?::([0-5][0-9])(?:\\.([0-9]{1,9}))?)?");
    @SerializedName("id") private JsonElement id;
    @SerializedName("robotId") private JsonElement robotId;
    @SerializedName("time") private JsonElement time;
    @SerializedName("enabled") private JsonElement enabled;
    @SerializedName("repeatSchedules") private List<RepeatSchedule> repeatSchedules;

    private static final class RepeatSchedule {
        @SerializedName("id") JsonElement id;
        @SerializedName("scheduleId") JsonElement scheduleId;
        @SerializedName("dayOfWeek") JsonElement dayOfWeek;
    }

    public boolean isValidFor(long expectedRobotId) {
        try {
            if (getId() <= 0 || getRobotId() <= 0 || getRobotId() != expectedRobotId || repeatSchedules == null) return false;
            getTime();
            isEnabled();
            Set<Long> ids = new HashSet<>();
            Set<Long> days = new HashSet<>();
            for (RepeatSchedule repeat : repeatSchedules) {
                if (repeat == null || integer(repeat.id) <= 0 || integer(repeat.scheduleId) != getId()
                        || integer(repeat.dayOfWeek) < 1 || integer(repeat.dayOfWeek) > 7
                        || !ids.add(integer(repeat.id)) || !days.add(integer(repeat.dayOfWeek))) return false;
            }
            return true;
        } catch (IllegalArgumentException | ArithmeticException error) { return false; }
    }

    static String normalizeTime(String value) {
        if (value == null) throw new IllegalArgumentException("Time is required");
        Matcher match = TIME.matcher(value);
        if (!match.matches()) throw new IllegalArgumentException("Invalid local time");
        String fraction = match.group(4);
        if (fraction != null) fraction = fraction.replaceFirst("0+$", "");
        return match.group(1) + ":" + match.group(2) + ":" + (match.group(3) == null ? "00" : match.group(3))
                + (fraction == null || fraction.isEmpty() ? "" : "." + fraction);
    }

    public long getId() { return integer(id); }
    public long getRobotId() { return integer(robotId); }
    public String getTime() { return normalizeTime(string(time)); }
    public boolean isEnabled() { return booleanValue(enabled); }
    public List<Integer> getDaysOfWeek() {
        Set<Integer> days = new TreeSet<>();
        for (RepeatSchedule repeat : repeatSchedules) days.add(Math.toIntExact(integer(repeat.dayOfWeek)));
        return Collections.unmodifiableList(new ArrayList<>(days));
    }
}
