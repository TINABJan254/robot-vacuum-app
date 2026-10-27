package com.ht.robotvacuumapp.viewmodel.robot;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ht.robotvacuumapp.viewmodel.robot.RobotControlUiState.Status;

public class RobotControlViewModel extends ViewModel {
    private final MutableLiveData<RobotControlUiState> state;

    public RobotControlViewModel(String name, int battery) {
        this(null, name, battery);
    }

    public RobotControlViewModel(String robotId, String name, int battery) {
        state = new MutableLiveData<>(new RobotControlUiState(
                robotId, name == null || name.isEmpty() ? "Robot_01" : name, battery, true, Status.WORKING, null, false, false));
    }

    public LiveData<RobotControlUiState> getUiState() { return state; }

    public void toggleMode() {
        RobotControlUiState s = state.getValue();
        if (s.robotUnpaired) return;
        state.setValue(new RobotControlUiState(s.robotId, s.robotName, s.batteryPercent, !s.autoMode, s.status,
                s.autoMode ? "Chuyển sang chế độ Thủ công (Manual)" : "Chuyển sang chế độ Tự động (Auto)", s.nameUpdated, s.robotUnpaired));
    }

    public void toggleWorking() {
        RobotControlUiState s = state.getValue();
        if (s.robotUnpaired) return;
        boolean working = s.status != Status.WORKING;
        state.setValue(new RobotControlUiState(s.robotId, s.robotName, s.batteryPercent, s.autoMode,
                working ? Status.WORKING : Status.PAUSED,
                working ? "Robot bắt đầu hoạt động (Working)" : "Robot đã tạm dừng (Paused)", s.nameUpdated, s.robotUnpaired));
    }

    public void returnToDock() {
        RobotControlUiState s = state.getValue();
        if (s.robotUnpaired) return;
        state.setValue(new RobotControlUiState(s.robotId, s.robotName, s.batteryPercent, s.autoMode,
                Status.CHARGING, "Robot đang quay về trạm sạc...", s.nameUpdated, s.robotUnpaired));
    }

    public void acknowledgeFeedback() {
        RobotControlUiState s = state.getValue();
        state.setValue(new RobotControlUiState(s.robotId, s.robotName, s.batteryPercent, s.autoMode, s.status, null,
                s.nameUpdated, s.robotUnpaired));
    }

    public void applyRenamedRobot(String id, String name) {
        RobotControlUiState s = state.getValue();
        if (s.robotUnpaired || s.robotId == null || !s.robotId.equals(id) || name == null || name.trim().isEmpty() || name.length() > 255) return;
        state.setValue(new RobotControlUiState(s.robotId, name, s.batteryPercent, s.autoMode, s.status, s.message, true, false));
    }

    public void applyUnpairedRobot(String id) {
        RobotControlUiState s = state.getValue();
        if (s.robotUnpaired || s.robotId == null || !s.robotId.equals(id)) return;
        state.setValue(new RobotControlUiState(s.robotId, s.robotName, s.batteryPercent, s.autoMode, s.status, null, false, true));
    }
}
