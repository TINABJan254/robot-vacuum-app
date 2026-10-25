package com.ht.robotvacuumapp.viewmodel.robot;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ht.robotvacuumapp.viewmodel.robot.RobotControlUiState.Status;

public class RobotControlViewModel extends ViewModel {
    private final MutableLiveData<RobotControlUiState> state;

    public RobotControlViewModel(String name, int battery) {
        state = new MutableLiveData<>(new RobotControlUiState(
                name == null || name.isEmpty() ? "Robot_01" : name, battery, true, Status.WORKING, null));
    }

    public LiveData<RobotControlUiState> getUiState() { return state; }

    public void toggleMode() {
        RobotControlUiState s = state.getValue();
        state.setValue(new RobotControlUiState(s.robotName, s.batteryPercent, !s.autoMode, s.status,
                s.autoMode ? "Chuyển sang chế độ Thủ công (Manual)" : "Chuyển sang chế độ Tự động (Auto)"));
    }

    public void toggleWorking() {
        RobotControlUiState s = state.getValue();
        boolean working = s.status != Status.WORKING;
        state.setValue(new RobotControlUiState(s.robotName, s.batteryPercent, s.autoMode,
                working ? Status.WORKING : Status.PAUSED,
                working ? "Robot bắt đầu hoạt động (Working)" : "Robot đã tạm dừng (Paused)"));
    }

    public void returnToDock() {
        RobotControlUiState s = state.getValue();
        state.setValue(new RobotControlUiState(s.robotName, s.batteryPercent, s.autoMode,
                Status.CHARGING, "Robot đang quay về trạm sạc..."));
    }

    public void acknowledgeFeedback() {
        RobotControlUiState s = state.getValue();
        state.setValue(new RobotControlUiState(s.robotName, s.batteryPercent, s.autoMode, s.status, null));
    }
}
