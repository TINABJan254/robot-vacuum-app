package com.ht.robotvacuumapp.api.dto;

import com.google.gson.annotations.SerializedName;
import com.google.gson.JsonElement;

public final class RobotResponse {
    @SerializedName("id") private Long id;
    @SerializedName("name") private String name;
    @SerializedName("userId") private JsonElement userId;

    // The backend also returns pairCode; it is not needed for display or stored locally.
    public Long getId() { return id; }
    public String getName() { return name; }
    public Long getUserId() {
        if (userId == null || !userId.isJsonPrimitive() || !userId.getAsJsonPrimitive().isNumber()) return null;
        try {
            return userId.getAsBigDecimal().longValueExact();
        } catch (NumberFormatException | ArithmeticException error) {
            return null;
        }
    }

    // Explicit JSON null confirms unpair; a missing userId does not.
    public boolean isUnpaired() { return userId != null && userId.isJsonNull(); }
}
