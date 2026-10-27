package com.ht.robotvacuumapp.api.dto;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.ht.robotvacuumapp.model.entity.RobotTelemetry;
import static com.ht.robotvacuumapp.api.dto.JsonResponseValues.*;

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
            timestamp(sent);
            timestamp(received);
            return new RobotTelemetry(id, (int) battery, RobotTelemetry.Status.valueOf(string(status)),
                    mode.isJsonNull() ? null : RobotTelemetry.Mode.valueOf(string(mode)),
                    sent, received, stale.getAsBoolean());
        } catch (IllegalArgumentException | ArithmeticException error) {
            return null;
        }
    }

}
