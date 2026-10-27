package com.ht.robotvacuumapp.model.entity;

import java.util.Objects;

/** Factories only expose combinations supported by the command API. */
public final class RobotCommand {
    public enum Action { START, STOP, SET_MODE, MOVE, STOP_MOVEMENT, RETURN_TO_DOCK }
    public enum Direction { FORWARD, BACKWARD, LEFT, RIGHT }

    public final Action action;
    public final RobotTelemetry.Mode mode;
    public final Direction direction;

    private RobotCommand(Action action, RobotTelemetry.Mode mode, Direction direction) {
        this.action = action;
        this.mode = mode;
        this.direction = direction;
    }

    /** Null mode intentionally uses the backend's MANUAL default. */
    public static RobotCommand start(RobotTelemetry.Mode mode) { return new RobotCommand(Action.START, mode, null); }
    public static RobotCommand stop() { return new RobotCommand(Action.STOP, null, null); }
    public static RobotCommand setMode(RobotTelemetry.Mode mode) {
        return new RobotCommand(Action.SET_MODE, Objects.requireNonNull(mode), null);
    }
    public static RobotCommand move(Direction direction) {
        return new RobotCommand(Action.MOVE, null, Objects.requireNonNull(direction));
    }
    public static RobotCommand stopMovement() { return new RobotCommand(Action.STOP_MOVEMENT, null, null); }
    public static RobotCommand returnToDock() { return new RobotCommand(Action.RETURN_TO_DOCK, null, null); }

    public RobotTelemetry.Mode responseMode() {
        return action == Action.START && mode == null ? RobotTelemetry.Mode.MANUAL : mode;
    }
}
