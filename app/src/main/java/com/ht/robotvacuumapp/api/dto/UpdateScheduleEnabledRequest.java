package com.ht.robotvacuumapp.api.dto;

import com.google.gson.annotations.SerializedName;

public final class UpdateScheduleEnabledRequest {
    @SerializedName("enabled") private final boolean enabled;
    public UpdateScheduleEnabledRequest(boolean enabled) { this.enabled = enabled; }
}
