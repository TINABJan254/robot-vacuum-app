package com.ht.robotvacuumapp.model.repository;

import com.ht.robotvacuumapp.model.entity.RobotTelemetry;

/**
 * Live telemetry pushed by the backend (WebSocket). Implementations deliver every callback on the
 * main thread and never after {@link #stop()} returned.
 */
public interface TelemetryStream {

    interface Listener {
        /** Latest snapshot; {@code null} means the backend has no reading for this robot yet. */
        void onTelemetry(RobotTelemetry snapshot);

        /** Connection dropped; the stream reconnects by itself (with backoff). */
        void onConnectionLost(String message);

        /** The access token was rejected. The stream has stopped. */
        void onUnauthorized();

        /** Access to the robot was refused (e.g. 403/404). The stream has stopped. */
        void onRejected(int httpStatus, String message);
    }

    /** Opens (or re-opens) the stream for one robot, replacing any previous subscription. */
    void start(long robotId, Listener listener);

    /** Closes the stream; no callback is delivered afterwards. Safe to call repeatedly. */
    void stop();
}
