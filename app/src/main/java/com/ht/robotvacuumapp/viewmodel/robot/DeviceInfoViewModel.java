package com.ht.robotvacuumapp.viewmodel.robot;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ht.robotvacuumapp.api.dto.RobotResponse;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.RobotRepository;
import java.util.Map;

public class DeviceInfoViewModel extends ViewModel {
    private final RobotRepository robots;
    private final AuthRepository auth;
    private final Long id;
    private final MutableLiveData<DeviceInfoUiState> state;
    private boolean cleared;
    private long operation;

    public DeviceInfoViewModel(String robotId, String initialName, RobotRepository robots, AuthRepository auth) {
        this.robots = robots;
        this.auth = auth;
        id = parseId(robotId);
        state = new MutableLiveData<>(new DeviceInfoUiState(id == null ? null : String.valueOf(id),
                initialName == null || initialName.trim().isEmpty() ? null : initialName, null, false,
                id == null ? "Thiếu ID robot hợp lệ. Vui lòng chọn lại robot từ danh sách." : null, false));
        if (id != null) loadRobot();
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

    public LiveData<DeviceInfoUiState> getUiState() { return state; }

    /** Called once at creation; explicit retry is allowed after a failed read. No polling. */
    public void loadRobot() {
        DeviceInfoUiState s = state.getValue();
        if (cleared || id == null || s.isLoading || s.requiresLogin) return;
        long current = ++operation;
        state.setValue(new DeviceInfoUiState(s.robotId, s.robotName, s.pairCode, true, null, false));
        robots.getRobot(id, new RobotRepository.Callback<RobotResponse>() {
            @Override public void onSuccess(RobotResponse result) {
                if (!isCurrent(current)) return;
                state.setValue(new DeviceInfoUiState(String.valueOf(result.getId()), result.getName(),
                        result.getPairCode(), false, null, false));
            }
            @Override public void onError(int status, String message, Map<String, String> fields) {
                if (!isCurrent(current)) return;
                if (status == 401) {
                    ++operation;
                    robots.cancelPendingRequests();
                    auth.cancelPendingRequests();
                    auth.logout();
                    state.setValue(new DeviceInfoUiState(null, null, null, false, message, true));
                    return;
                }
                DeviceInfoUiState latest = state.getValue();
                state.setValue(new DeviceInfoUiState(latest.robotId, latest.robotName, latest.pairCode,
                        false, message, false));
            }
        });
    }

    private boolean isCurrent(long current) {
        return !cleared && operation == current && !state.getValue().requiresLogin;
    }

    @Override protected void onCleared() {
        cleared = true;
        ++operation;
        robots.cancelPendingRequests();
        auth.cancelPendingRequests();
    }
}
