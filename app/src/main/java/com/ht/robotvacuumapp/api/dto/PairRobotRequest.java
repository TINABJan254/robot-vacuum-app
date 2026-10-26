package com.ht.robotvacuumapp.api.dto;

import com.google.gson.annotations.SerializedName;

public final class PairRobotRequest {
    @SerializedName("robotId") private final long robotId;
    @SerializedName("pairCode") private final String pairCode;

    public PairRobotRequest(long robotId, String pairCode) {
        this.robotId = robotId;
        this.pairCode = pairCode;
    }
}
