package com.ht.robotvacuumapp.api.dto;

import com.google.gson.annotations.SerializedName;

public final class UpdateAlertReadAtRequest {
    @SerializedName("readAt") private final String readAt;

    public UpdateAlertReadAtRequest(String readAt) {
        if (readAt == null) throw new IllegalArgumentException("Read timestamp is required");
        JsonResponseValues.timestamp(readAt);
        this.readAt = readAt;
    }
}
