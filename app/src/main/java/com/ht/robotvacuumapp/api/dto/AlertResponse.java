package com.ht.robotvacuumapp.api.dto;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.nio.charset.StandardCharsets;
import static com.ht.robotvacuumapp.api.dto.JsonResponseValues.*;

public final class AlertResponse {
    @SerializedName("id") private JsonElement id;
    @SerializedName("title") private JsonElement title;
    @SerializedName("message") private JsonElement message;
    @SerializedName("readAt") private JsonElement readAt;
    @SerializedName("createdAt") private JsonElement createdAt;
    @SerializedName("robot") private RobotSummary robot;

    private static final class RobotSummary {
        @SerializedName("id") JsonElement id;
        @SerializedName("name") JsonElement name;
    }

    public boolean isValid() {
        try {
            if (getId() <= 0 || !shortText(getTitle()) || message == null || readAt == null || robot == null
                    || getRobotId() <= 0 || !shortText(getRobotName())) return false;
            String body = getMessage();
            if (body != null && body.getBytes(StandardCharsets.UTF_8).length > 65_535) return false;
            timestamp(getCreatedAt());
            if (!isUnread()) timestamp(getReadAt());
            return true;
        } catch (IllegalArgumentException | ArithmeticException error) {
            return false;
        }
    }

    private static boolean shortText(String value) { return !value.trim().isEmpty() && value.length() <= 255; }
    public long getId() { return integer(id); }
    public String getTitle() { return string(title); }
    public String getMessage() { return message.isJsonNull() ? null : string(message); }
    public String getReadAt() { return readAt.isJsonNull() ? null : string(readAt); }
    public boolean isUnread() { return getReadAt() == null; }
    public String getCreatedAt() { return string(createdAt); }
    public long getCreatedAtMillis() { return timestamp(getCreatedAt()).movePointRight(3).longValue(); }
    public long getRobotId() { return integer(robot.id); }
    public String getRobotName() { return string(robot.name); }
}
