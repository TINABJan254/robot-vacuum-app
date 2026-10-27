package com.ht.robotvacuumapp.api.dto;

import com.google.gson.annotations.SerializedName;

public final class UpdateRobotNameRequest {
    @SerializedName("name") private final String name;

    public UpdateRobotNameRequest(String name) { this.name = name; }
}
