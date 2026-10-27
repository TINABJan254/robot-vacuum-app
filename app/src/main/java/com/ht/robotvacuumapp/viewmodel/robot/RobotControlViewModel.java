package com.ht.robotvacuumapp.viewmodel.robot;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.RobotRepository;
import java.util.Map;

public class RobotControlViewModel extends ViewModel {
    public static final long POLL_INTERVAL_MS = 5_000;

    public interface Scheduler {
        void postDelayed(Runnable task, long delayMillis);
        void remove(Runnable task);
    }

    private static final class MainScheduler implements Scheduler {
        private final Handler handler = new Handler(Looper.getMainLooper());
        @Override public void postDelayed(Runnable task, long delay) { handler.postDelayed(task, delay); }
        @Override public void remove(Runnable task) { handler.removeCallbacks(task); }
    }

    private final RobotRepository robots;
    private final AuthRepository auth;
    private final Scheduler scheduler;
    private final Long id;
    private final MutableLiveData<RobotControlUiState> state;
    private final Runnable poll = this::loadTelemetry;
    private boolean active;
    private boolean pollingAllowed;
    private boolean cleared;
    private long operation;

    public RobotControlViewModel(String robotId, String name, RobotRepository robots, AuthRepository auth) {
        this(robotId, name, robots, auth, new MainScheduler());
    }

    public RobotControlViewModel(String robotId, String name, RobotRepository robots,
                                 AuthRepository auth, Scheduler scheduler) {
        this.robots = robots;
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

    public void startTelemetry() {
        if (active || !canRead()) return;
        active = true;
        pollingAllowed = true;
        loadTelemetry();
    }

    public void stopTelemetry() {
        cancelPolling();
        if (state.getValue().isLoading) {
            RobotControlUiState.Builder b = state.getValue().toBuilder();
            b.isLoading = false;
            state.setValue(b.build());
        }
    }

    private void cancelPolling() {
        active = false;
        pollingAllowed = false;
        ++operation;
        scheduler.remove(poll);
        robots.cancelPendingRequests();
    }

    public void retryTelemetry() {
        if (!active || !canRead() || state.getValue().isLoading) return;
        pollingAllowed = true;
        scheduler.remove(poll);
        loadTelemetry();
    }

    private boolean canRead() {
        RobotControlUiState s = state.getValue();
        return !cleared && id != null && !s.robotUnpaired && !s.requiresLogin;
    }

    private void loadTelemetry() {
        if (!active || !pollingAllowed || !canRead() || state.getValue().isLoading) return;
        long current = ++operation;
        RobotControlUiState.Builder loading = state.getValue().toBuilder();
        loading.isLoading = true;
        // Keep the last error visible until a successful read, including while retrying.
        state.setValue(loading.build());
        robots.getTelemetry(id, new RobotRepository.Callback<RobotRepository.TelemetryResult>() {
            @Override public void onSuccess(RobotRepository.TelemetryResult result) {
                if (!isCurrent(current)) return;
                RobotControlUiState.Builder b = state.getValue().toBuilder();
                b.telemetry = result.snapshot; // HTTP 204 explicitly clears the previous snapshot.
                b.telemetryLoaded = true;
                b.isLoading = false;
                b.telemetryError = null;
                state.setValue(b.build());
                scheduleNext();
            }
            @Override public void onError(int status, String message, Map<String, String> fields) {
                if (!isCurrent(current)) return;
                if (status == 401) {
                    expireSession(message);
                    return;
                }
                if (status == 400 || status == 403 || status == 404) pollingAllowed = false;
                RobotControlUiState.Builder b = state.getValue().toBuilder();
                b.isLoading = false;
                b.telemetryError = message;
                state.setValue(b.build());
                scheduleNext();
            }
        });
    }

    private boolean isCurrent(long current) {
        return active && canRead() && current == operation;
    }

    private void scheduleNext() {
        scheduler.remove(poll);
        if (active && pollingAllowed && canRead()) scheduler.postDelayed(poll, POLL_INTERVAL_MS);
    }

    private void expireSession(String message) {
        cancelPolling();
        auth.cancelPendingRequests();
        auth.logout();
        RobotControlUiState.Builder b = state.getValue().toBuilder();
        b.telemetry = null;
        b.isLoading = false;
        b.telemetryError = null;
        b.requiresLogin = true;
        b.message = message;
        state.setValue(b.build());
    }

    public void showControlUnavailable() {
        RobotControlUiState s = state.getValue();
        if (cleared || s.robotUnpaired || s.requiresLogin) return;
        RobotControlUiState.Builder b = s.toBuilder();
        b.message = "Chức năng gửi lệnh điều khiển chưa được tích hợp.";
        state.setValue(b.build());
    }

    public void acknowledgeFeedback() {
        RobotControlUiState.Builder b = state.getValue().toBuilder();
        b.message = null;
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
        cancelPolling();
        RobotControlUiState.Builder b = s.toBuilder();
        b.robotUnpaired = true;
        b.nameUpdated = false;
        b.isLoading = false;
        b.telemetry = null;
        b.telemetryError = null;
        b.message = null;
        state.setValue(b.build());
    }

    @Override protected void onCleared() {
        cleared = true;
        cancelPolling();
        auth.cancelPendingRequests();
    }
}
