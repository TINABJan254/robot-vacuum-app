package com.ht.robotvacuumapp.api.dto;

import com.google.gson.annotations.SerializedName;
import com.ht.robotvacuumapp.model.entity.RobotCommand;
import com.ht.robotvacuumapp.model.entity.RobotTelemetry;
import java.util.Objects;

/** Gson omits null fields, so each action sends only its supported parameters. */
public final class RobotCommandRequest {
    @SerializedName("action") private final RobotCommand.Action action;
    @SerializedName("mode") private final RobotTelemetry.Mode mode;
    @SerializedName("direction") private final RobotCommand.Direction direction;

    public RobotCommandRequest(RobotCommand command) {
        action = Objects.requireNonNull(command).action;
        mode = command.mode;
        direction = command.direction;
    }
}
