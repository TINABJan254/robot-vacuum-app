package com.ht.robotvacuumapp.api.dto;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.ht.robotvacuumapp.model.entity.RobotTelemetry;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Locale;

/** Validate JSON types explicitly: Gson otherwise coerces strings into numbers/booleans. */
public final class RobotTelemetryResponse {
    @SerializedName("robotId") private JsonElement robotId;
    @SerializedName("batteryPercent") private JsonElement batteryPercent;
    @SerializedName("status") private JsonElement status;
    @SerializedName("mode") private JsonElement mode;
    @SerializedName("sentAt") private JsonElement sentAt;
    @SerializedName("receivedAt") private JsonElement receivedAt;
    @SerializedName("stale") private JsonElement stale;

    public RobotTelemetry toSnapshot(long expectedId) {
        try {
            long id = integer(robotId);
            long battery = integer(batteryPercent);
            if (id <= 0 || id != expectedId || battery < 0 || battery > 100
                    || mode == null || stale == null || !stale.isJsonPrimitive()
                    || !stale.getAsJsonPrimitive().isBoolean()) return null;
            String sent = string(sentAt);
            String received = string(receivedAt);
            if (!validTimestamp(sent) || !validTimestamp(received)) return null;
            return new RobotTelemetry(id, (int) battery, RobotTelemetry.Status.valueOf(string(status)),
                    mode.isJsonNull() ? null : RobotTelemetry.Mode.valueOf(string(mode)),
                    sent, received, stale.getAsBoolean());
        } catch (IllegalArgumentException | ArithmeticException error) {
            return null;
        }
    }

    private static long integer(JsonElement value) {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("Expected integer");
        }
        return value.getAsBigDecimal().longValueExact();
    }

    private static String string(JsonElement value) {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Expected string");
        }
        return value.getAsString();
    }

    private static boolean validTimestamp(String value) {
        if (!value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]{1,9})?(Z|[+-][0-9]{2}:[0-9]{2})")) {
            return false;
        }
        // SimpleDateFormat is available on minSdk 24; validate fractional ISO timestamps
        // after removing the fraction rather than adding a java.time desugaring dependency.
        String seconds = value.replaceFirst("\\.[0-9]{1,9}", "");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        return format.parse(seconds, position) != null && position.getIndex() == seconds.length();
    }
}
