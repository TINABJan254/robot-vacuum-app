package com.ht.robotvacuumapp.viewmodel.robot;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.RobotRepository;
import com.ht.robotvacuumapp.model.repository.TelemetryStream;
import com.ht.robotvacuumapp.model.entity.RobotCommand;
import com.ht.robotvacuumapp.api.dto.RobotCommandResponse;
import com.ht.robotvacuumapp.model.entity.RobotTelemetry;
import java.util.Map;

/**
 * Robot control screen.
 * <ul>
 *   <li>Telemetry is pushed by the backend over WebSocket ({@link TelemetryStream}); no polling.</li>
 *   <li>D-pad is hold-to-move: while a direction is held, MOVE is re-sent every {@link #MOVE_REPEAT_MS};
 *   releasing sends STOP_MOVEMENT. The firmware stops by itself if MOVE is not repeated in time.</li>
 * </ul>
 */
public class RobotControlViewModel extends ViewModel {
    /** Re-send interval while a D-pad direction is held. Must stay well below the firmware timeout (800 ms). */
    public static final long MOVE_REPEAT_MS = 250;

    public interface Scheduler {
        void postDelayed(Runnable task, long delayMillis);
        void remove(Runnable task);
    }

    private static final class MainScheduler implements Scheduler {
        private final Handler handler = new Handler(Looper.getMainLooper());
        @Override public void postDelayed(Runnable task, long delay) { handler.postDelayed(task, delay); }
        @Override public void remove(Runnable task) { handler.removeCallbacks(task); }
    }

    private final TelemetryStream telemetryStream;
    private final RobotRepository commands;
    private final AuthRepository auth;
    private final Scheduler scheduler;
    private final Long id;
    private final MutableLiveData<RobotControlUiState> state;
    private boolean active;
    private boolean cleared;
    private long commandOperation;

    // Hold-to-move state.
    private final Runnable repeatMove = this::moveTick;
    private RobotCommand.Direction holdDirection;
    private RobotCommand.Direction lastSentDirection;
    private boolean moveInFlight;
    private boolean stopAfterMove;
    private boolean holdErrorShown;

    private final TelemetryStream.Listener telemetryListener = new TelemetryStream.Listener() {
        @Override public void onTelemetry(RobotTelemetry snapshot) {
            if (!active || !canRead()) return;
            RobotControlUiState.Builder b = state.getValue().toBuilder();
            b.telemetry = snapshot; // null: the backend has no reading yet.
            b.telemetryLoaded = true;
            b.isLoading = false;
            b.telemetryError = null;
            b.live = true;
            state.setValue(b.build());
        }

        @Override public void onConnectionLost(String message) {
            if (!active || !canRead()) return;
            RobotControlUiState.Builder b = state.getValue().toBuilder();
            b.isLoading = false;
            b.live = false;
            b.telemetryError = message; // Last snapshot stays visible while reconnecting.
            state.setValue(b.build());
        }

        @Override public void onUnauthorized() {
            if (cleared) return;
            expireSession("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }

        @Override public void onRejected(int httpStatus, String message) {
            if (!canRead()) return;
            RobotControlUiState.Builder b = state.getValue().toBuilder();
            b.isLoading = false;
            b.live = false;
            b.telemetryError = message;
            state.setValue(b.build());
        }
    };

    public RobotControlViewModel(String robotId, String name, TelemetryStream telemetryStream,
                                 RobotRepository commands, AuthRepository auth) {
        this(robotId, name, telemetryStream, commands, auth, new MainScheduler());
    }

    public RobotControlViewModel(String robotId, String name, TelemetryStream telemetryStream,
                                 RobotRepository commands, AuthRepository auth, Scheduler scheduler) {
        this.telemetryStream = telemetryStream;
        this.commands = commands;
        this.auth = auth;
        this.scheduler = scheduler;
        id = parseId(robotId);
        RobotControlUiState.Builder initial = new RobotControlUiState.Builder();
        initial.robotId = id == null ? null : String.valueOf(id);
        initial.robotName = name == null || name.trim().isEmpty() ? "Robot" : name;
        if (id == null) initial.telemetryError = "Thiếu ID robot hợp lệ. Vui lòng chọn lại robot từ danh sách.";
        state = new MutableLiveData<>(initial.build());
    }

    private Long parseId(String value) {
        try {
            if (value == null || !value.matches("[0-9]+")) return null;
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException error) {
            return null;
        }
    }

    public LiveData<RobotControlUiState> getUiState() { return state; }

    // ------------------------------------------------------------------
    //  Telemetry (WebSocket)
    // ------------------------------------------------------------------

    public void startTelemetry() {
        if (active || !canRead()) return;
        active = true;
        openStream();
    }

    public void stopTelemetry() {
        active = false;
        telemetryStream.stop();
        RobotControlUiState s = state.getValue();
        if (s.isLoading || s.live) {
            RobotControlUiState.Builder b = s.toBuilder();
            b.isLoading = false;
            b.live = false;
            state.setValue(b.build());
        }
    }

    /** Re-opens the stream after it was rejected (e.g. a transient 403/404 during re-pairing). */
    public void retryTelemetry() {
        if (!active || !canRead() || state.getValue().isLoading) return;
        openStream();
    }

    private void openStream() {
        RobotControlUiState.Builder b = state.getValue().toBuilder();
        b.isLoading = true;
        state.setValue(b.build());
        telemetryStream.start(id, telemetryListener);
    }

    private boolean canRead() {
        RobotControlUiState s = state.getValue();
        return !cleared && id != null && !s.robotUnpaired && !s.requiresLogin;
    }

    private void expireSession(String message) {
        active = false;
        telemetryStream.stop();
        cancelCommands();
        auth.cancelPendingRequests();
        auth.logout();
        RobotControlUiState.Builder b = state.getValue().toBuilder();
        b.telemetry = null;
        b.isLoading = false;
        b.live = false;
        b.telemetryError = null;
        b.requiresLogin = true;
        clearCommandState(b);
        b.message = message;
        state.setValue(b.build());
    }

    // ------------------------------------------------------------------
    //  One-shot commands (start/stop, mode, dock)
    // ------------------------------------------------------------------

    public void toggleWorking() {
        RobotTelemetry t = state.getValue().telemetry;
        sendCommand(t != null && t.status == RobotTelemetry.Status.WORKING
                ? RobotCommand.stop() : RobotCommand.start(t == null ? null : t.mode));
    }

    public void toggleMode() {
        RobotTelemetry t = state.getValue().telemetry;
        if (t == null || t.mode == null) return;
        sendCommand(RobotCommand.setMode(t.mode == RobotTelemetry.Mode.AUTO
                ? RobotTelemetry.Mode.MANUAL : RobotTelemetry.Mode.AUTO));
    }

    public void returnToDock() { sendCommand(RobotCommand.returnToDock()); }

    private void sendCommand(RobotCommand command) {
        if (!active || !canRead() || state.getValue().isSendingCommand) return;
        long current = ++commandOperation;
        RobotControlUiState.Builder b = state.getValue().toBuilder();
        b.isSendingCommand = true;
        b.pendingCommand = command;
        b.publishedCommand = null;
        b.message = null;
        state.setValue(b.build());
        commands.sendCommand(id, command, new RobotRepository.Callback<RobotCommandResponse>() {
            @Override public void onSuccess(RobotCommandResponse response) {
                if (!isCurrentCommand(current)) return;
                RobotControlUiState.Builder next = state.getValue().toBuilder();
                next.isSendingCommand = false;
                next.pendingCommand = null;
                next.publishedCommand = response.getAction();
                state.setValue(next.build());
            }
            @Override public void onError(int status, String message, Map<String, String> fields) {
                if (!isCurrentCommand(current)) return;
                if (status == 401) {
                    expireSession(message);
                    return;
                }
                RobotControlUiState.Builder next = state.getValue().toBuilder();
                next.isSendingCommand = false;
                next.pendingCommand = null;
                next.publishedCommand = null;
                next.message = message;
                state.setValue(next.build());
            }
        });
    }

    private boolean isCurrentCommand(long current) {
        // A request survives onStop/recreation; a cleared, expired or unpaired screen does not.
        return canRead() && current == commandOperation && state.getValue().isSendingCommand;
    }

    // ------------------------------------------------------------------
    //  D-pad: hold-to-move
    // ------------------------------------------------------------------

    /** Finger down on a direction (or slid to another one): move now and keep repeating. */
    public void startMove(RobotCommand.Direction direction) {
        if (direction == null || !canRead()) return;
        if (direction == holdDirection) return;
        holdDirection = direction;
        holdErrorShown = false;
        stopAfterMove = false;
        scheduler.remove(repeatMove);
        moveTick();
    }

    /** Finger up / cancelled / screen hidden: stop the wheels right away. No-op if nothing is held. */
    public void stopMove() {
        if (holdDirection == null) return;
        holdDirection = null;
        scheduler.remove(repeatMove);
        sendStopMovement(false);
    }

    /** Finger lifted from one direction button; ignored if another direction has taken over (multi-touch). */
    public void stopMove(RobotCommand.Direction released) {
        if (released != null && released == holdDirection) stopMove();
    }

    /** Center button: explicit stop, never blocked by other pending commands. */
    public void stopMovement() {
        holdDirection = null;
        scheduler.remove(repeatMove);
        sendStopMovement(true);
    }

    public boolean isHoldingMove() { return holdDirection != null; }

    private void moveTick() {
        if (holdDirection == null) return;
        if (!canRead()) {
            holdDirection = null;
            return;
        }
        // At most one MOVE in flight; a slow network skips ticks instead of queueing them.
        if (!moveInFlight) sendMove(holdDirection);
        scheduler.postDelayed(repeatMove, MOVE_REPEAT_MS);
    }

    private void sendMove(RobotCommand.Direction direction) {
        moveInFlight = true;
        lastSentDirection = direction;
        commands.sendCommand(id, RobotCommand.move(direction), new RobotRepository.Callback<RobotCommandResponse>() {
            @Override public void onSuccess(RobotCommandResponse response) { moveFinished(); }
            @Override public void onError(int status, String message, Map<String, String> fields) {
                moveInFlight = false;
                if (cleared) return;
                if (status == 401) {
                    expireSession(message);
                    return;
                }
                if (holdDirection != null && !holdErrorShown) {
                    holdErrorShown = true; // One message per press, not one per repeat.
                    showMessage(message);
                }
                moveFinished();
            }
        });
    }

    private void moveFinished() {
        moveInFlight = false;
        if (cleared || !canRead()) return;
        if (stopAfterMove) {
            // A MOVE answered after the release may have reached the robot after our STOP: stop again.
            stopAfterMove = false;
            sendStopMovement(false);
        } else if (holdDirection != null && holdDirection != lastSentDirection) {
            sendMove(holdDirection); // Direction changed while the previous MOVE was in flight.
        }
    }

    private void sendStopMovement(boolean feedback) {
        if (!canRead()) return;
        if (moveInFlight) stopAfterMove = true;
        commands.sendCommand(id, RobotCommand.stopMovement(), new RobotRepository.Callback<RobotCommandResponse>() {
            @Override public void onSuccess(RobotCommandResponse response) {
                if (!feedback || cleared || !canRead()) return;
                RobotControlUiState.Builder b = state.getValue().toBuilder();
                b.publishedCommand = RobotCommand.Action.STOP_MOVEMENT;
                state.setValue(b.build());
            }
            @Override public void onError(int status, String message, Map<String, String> fields) {
                if (cleared) return;
                if (status == 401) {
                    expireSession(message);
                    return;
                }
                showMessage(message);
            }
        });
    }

    private void showMessage(String message) {
        if (!canRead()) return;
        RobotControlUiState.Builder b = state.getValue().toBuilder();
        b.message = message;
        state.setValue(b.build());
    }

    // ------------------------------------------------------------------
    //  Shared
    // ------------------------------------------------------------------

    private void cancelCommands() {
        ++commandOperation;
        holdDirection = null;
        moveInFlight = false;
        stopAfterMove = false;
        scheduler.remove(repeatMove);
        commands.cancelPendingRequests();
    }

    private void clearCommandState(RobotControlUiState.Builder b) {
        b.isSendingCommand = false;
        b.pendingCommand = null;
        b.publishedCommand = null;
    }

    public void acknowledgeFeedback() {
        RobotControlUiState.Builder b = state.getValue().toBuilder();
        b.message = null;
        b.publishedCommand = null;
        state.setValue(b.build());
    }

    public void applyRenamedRobot(String robotId, String name) {
        RobotControlUiState s = state.getValue();
        if (cleared || s.requiresLogin || s.robotUnpaired || s.robotId == null || !s.robotId.equals(robotId)
                || name == null || name.trim().isEmpty() || name.length() > 255) return;
        RobotControlUiState.Builder b = s.toBuilder();
        b.robotName = name;
        b.nameUpdated = true;
        state.setValue(b.build());
    }

    public void applyUnpairedRobot(String robotId) {
        RobotControlUiState s = state.getValue();
        if (cleared || s.requiresLogin || s.robotUnpaired || s.robotId == null || !s.robotId.equals(robotId)) return;
        active = false;
        telemetryStream.stop();
        cancelCommands();
        RobotControlUiState.Builder b = s.toBuilder();
        b.robotUnpaired = true;
        b.nameUpdated = false;
        b.isLoading = false;
        b.live = false;
        b.telemetry = null;
        b.telemetryError = null;
        b.message = null;
        clearCommandState(b);
        state.setValue(b.build());
    }

    @Override protected void onCleared() {
        cleared = true;
        active = false;
        telemetryStream.stop();
        cancelCommands();
        auth.cancelPendingRequests();
    }
}
