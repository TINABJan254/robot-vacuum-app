package com.ht.robotvacuumapp.api.dto;

import com.google.gson.annotations.SerializedName;

public final class RobotResponse {
    @SerializedName("id") private Long id;
    @SerializedName("name") private String name;
    @SerializedName("userId") private Long userId;

    // The backend also returns pairCode; it is not needed for display or stored locally.
    public Long getId() { return id; }
    public String getName() { return name; }
    public Long getUserId() { return userId; }
}
