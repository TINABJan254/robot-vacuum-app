package com.ht.robotvacuumapp.api.dto;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.ht.robotvacuumapp.model.entity.RobotCommand;
import com.ht.robotvacuumapp.model.entity.RobotTelemetry;
import static com.ht.robotvacuumapp.api.dto.JsonResponseValues.*;

public final class RobotCommandResponse {
    @SerializedName("commandId") private JsonElement commandId;
    @SerializedName("robotId") private JsonElement robotId;
    @SerializedName("action") private JsonElement action;
    @SerializedName("mode") private JsonElement mode;
    @SerializedName("direction") private JsonElement direction;
    @SerializedName("status") private JsonElement status;
    @SerializedName("issuedAt") private JsonElement issuedAt;
    @SerializedName("expiresAt") private JsonElement expiresAt;

    public boolean isValidFor(long expectedId, RobotCommand request) {
        try {
            if (request == null || mode == null || direction == null) return false;
            return !getCommandId().trim().isEmpty() && getRobotId() > 0 && getRobotId() == expectedId
                    && getAction() == request.action && getMode() == request.responseMode()
                    && getDirection() == request.direction && "PUBLISHED".equals(getStatus())
                    && timestamp(getExpiresAt()).compareTo(timestamp(getIssuedAt())) > 0;
        } catch (IllegalArgumentException | ArithmeticException error) {
            return false;
        }
    }

    // The repository validates the response before exposing these typed values.
    public String getCommandId() { return string(commandId); }
    public long getRobotId() { return integer(robotId); }
    public RobotCommand.Action getAction() { return RobotCommand.Action.valueOf(string(action)); }
    public RobotTelemetry.Mode getMode() {
        return mode.isJsonNull() ? null : RobotTelemetry.Mode.valueOf(string(mode));
    }
    public RobotCommand.Direction getDirection() {
        return direction.isJsonNull() ? null : RobotCommand.Direction.valueOf(string(direction));
    }
    public String getStatus() { return string(status); }
    public String getIssuedAt() { return string(issuedAt); }
    public String getExpiresAt() { return string(expiresAt); }
}
