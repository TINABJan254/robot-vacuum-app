package com.ht.robotvacuumapp.api.dto;

import com.google.gson.annotations.SerializedName;
import com.google.gson.JsonElement;

public final class RobotResponse {
    @SerializedName("id") private Long id;
    @SerializedName("name") private String name;
    @SerializedName("pairCode") private JsonElement pairCode;
    @SerializedName("userId") private JsonElement userId;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPairCode() {
        return pairCode != null && pairCode.isJsonPrimitive() && pairCode.getAsJsonPrimitive().isString()
                ? pairCode.getAsString() : null;
    }
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
